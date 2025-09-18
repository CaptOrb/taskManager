import * as cdk from 'aws-cdk-lib';
import * as autoscaling from 'aws-cdk-lib/aws-autoscaling';
import * as cloudfront from 'aws-cdk-lib/aws-cloudfront';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elasticloadbalancingv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as logs from 'aws-cdk-lib/aws-logs';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as s3 from 'aws-cdk-lib/aws-s3';

export interface TaskappStackProps extends cdk.StackProps {
  /**
   * The 'sub' claim for the GitHub OIDC token
   * @default 'repo:CaptOrb/taskManager:*'
   */
  readonly gitHubRepoSubClaim?: string;
}

/**
 * Full Stack POC for taskapp with ECS, RDS, S3, CloudFront using ECR for Docker images (using NAT Instance)
 */
export class TaskappStack extends cdk.Stack {
  /**
   * React app task-app (via CloudFront HTTPS)
   */
  public readonly frontendtaskapp;
  /**
   * RDS endpoint for backend app
   */
  public readonly rdsEndpoint;
  /**
   * ALB endpoint for backend app
   */
  public readonly albEndpoint;

  public constructor(scope: cdk.App, id: string, props: TaskappStackProps = {}) {
    super(scope, id, props);

    // Applying default props
    props = {
      ...props,
      gitHubRepoSubClaim: props.gitHubRepoSubClaim ?? 'repo:CaptOrb/taskManager:*',
    };

    // Resources
    const cloudFrontOac = new cloudfront.CfnOriginAccessControl(this, 'CloudFrontOAC', {
      originAccessControlConfig: {
        name: `${this.stackName}-FrontendOAC`,
        signingBehavior: 'always',
        signingProtocol: 'sigv4',
        originAccessControlOriginType: 's3',
      },
    });

    const ecsCluster = new ecs.CfnCluster(this, 'ECSCluster', {
      clusterName: `${this.stackName}-Cluster`,
    });

    const ecsInstanceRole = new iam.CfnRole(this, 'ECSInstanceRole', {
      assumeRolePolicyDocument: {
        Version: '2012-10-17',
        Statement: [
          {
            Effect: 'Allow',
            Principal: {
              Service: 'ec2.amazonaws.com',
            },
            Action: 'sts:AssumeRole',
          },
        ],
      },
      managedPolicyArns: [
        'arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role',
        'arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly',
        'arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy',
        'arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore',
      ],
    });

    const ecsLogGroup = new logs.CfnLogGroup(this, 'ECSLogGroup', {
      logGroupName: `/ecs/${this.stackName}-springboot-app`,
      retentionInDays: 7,
    });

    const fckNatRole = new iam.CfnRole(this, 'FckNatRole', {
      assumeRolePolicyDocument: {
        Version: '2012-10-17',
        Statement: [
          {
            Effect: 'Allow',
            Principal: {
              Service: [
                'ec2.amazonaws.com',
              ],
            },
            Action: [
              'sts:AssumeRole',
            ],
          },
        ],
      },
      managedPolicyArns: [
        'arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore',
      ],
    });

    const frontendBucket = new s3.CfnBucket(this, 'FrontendBucket', {
      bucketEncryption: {
        serverSideEncryptionConfiguration: [
          {
            serverSideEncryptionByDefault: {
              sseAlgorithm: 'AES256',
            },
          },
        ],
      },
      publicAccessBlockConfiguration: {
        blockPublicAcls: true,
        blockPublicPolicy: true,
        ignorePublicAcls: true,
        restrictPublicBuckets: true,
      },
    });

    const gitHubActionsOidcProvider = new iam.CfnOIDCProvider(this, 'GitHubActionsOidcProvider', {
      clientIdList: [
        'sts.amazonaws.com',
      ],
      thumbprintList: [
        '6938fd4d98bab03faadb97b34396831e3780aea1',
      ],
      url: 'https://token.actions.githubusercontent.com',
    });

    const internetGateway = new ec2.CfnInternetGateway(this, 'InternetGateway', {
      tags: [
        {
          key: 'Name',
          value: 'taskapp-InternetGateway',
        },
      ],
    });

    const taskExecutionRole = new iam.CfnRole(this, 'TaskExecutionRole', {
      assumeRolePolicyDocument: {
        Version: '2012-10-17',
        Statement: [
          {
            Effect: 'Allow',
            Principal: {
              Service: 'ecs-tasks.amazonaws.com',
            },
            Action: 'sts:AssumeRole',
          },
        ],
      },
      managedPolicyArns: [
        'arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy',
        'arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly',
      ],
      policies: [
        {
          policyName: 'TaskAppSecretsManagerAccess',
          policyDocument: {
            Version: '2012-10-17',
            Statement: [
              {
                Effect: 'Allow',
                Action: [
                  'secretsmanager:GetSecretValue',
                  'secretsmanager:DescribeSecret',
                ],
                Resource: `arn:aws:secretsmanager:${this.region}:${this.account}:secret:/taskapp/db-credentials-zY5O7P`,
              },
            ],
          },
        },
      ],
    });

    const vpc = new ec2.CfnVPC(this, 'VPC', {
      cidrBlock: '10.0.0.0/16',
      enableDnsSupport: true,
      enableDnsHostnames: true,
      tags: [
        {
          key: 'Name',
          value: 'taskapp-VPC',
        },
      ],
    });

    const albSecurityGroup = new ec2.CfnSecurityGroup(this, 'ALBSecurityGroup', {
      groupDescription: 'Allow HTTP traffic to ALB',
      vpcId: vpc.ref,
      securityGroupIngress: [
        {
          ipProtocol: 'tcp',
          fromPort: 80,
          toPort: 80,
          cidrIp: '0.0.0.0/0',
        },
        {
          ipProtocol: 'tcp',
          fromPort: 443,
          toPort: 443,
          cidrIp: '0.0.0.0/0',
        },
      ],
    });

    const attachGateway = new ec2.CfnVPCGatewayAttachment(this, 'AttachGateway', {
      vpcId: vpc.ref,
      internetGatewayId: internetGateway.ref,
    });

    const ecsSecurityGroup = new ec2.CfnSecurityGroup(this, 'ECSSecurityGroup', {
      groupDescription: 'Security Group for ECS host - allows essential outbound access for SSM, ECR, CloudWatch, and DNS',
      vpcId: vpc.ref,
      securityGroupEgress: [
        {
          description: 'Allow outbound HTTPS to AWS services (SSM, ECR, CloudWatch)',
          ipProtocol: 'tcp',
          fromPort: 443,
          toPort: 443,
          cidrIp: '0.0.0.0/0',
        },
        {
          description: 'Allow outbound for DNS resolution (TCP)',
          ipProtocol: 'tcp',
          fromPort: 53,
          toPort: 53,
          cidrIp: '0.0.0.0/0',
        },
        {
          description: 'Allow outbound for DNS resolution (UDP)',
          ipProtocol: 'udp',
          fromPort: 53,
          toPort: 53,
          cidrIp: '0.0.0.0/0',
        },
      ],
    });

    const ecsTargetGroup = new elasticloadbalancingv2.CfnTargetGroup(this, 'ECSTargetGroup', {
      name: 'taskapp-target-group',
      port: 8080,
      protocol: 'HTTP',
      vpcId: vpc.ref,
      targetType: 'ip',
      healthCheckPath: '/health',
      healthCheckIntervalSeconds: 60,
      healthCheckTimeoutSeconds: 5,
      healthyThresholdCount: 2,
      unhealthyThresholdCount: 3,
    });

    const fckNatInstanceProfile = new iam.CfnInstanceProfile(this, 'FckNatInstanceProfile', {
      roles: [
        fckNatRole.ref,
      ],
    });

    const instanceProfile = new iam.CfnInstanceProfile(this, 'InstanceProfile', {
      roles: [
        ecsInstanceRole.ref,
      ],
    });

    const privateRouteTable = new ec2.CfnRouteTable(this, 'PrivateRouteTable', {
      vpcId: vpc.ref,
      tags: [
        {
          key: 'Name',
          value: 'taskapp-PrivateRouteTable',
        },
      ],
    });

    const privateSubnet = new ec2.CfnSubnet(this, 'PrivateSubnet', {
      vpcId: vpc.ref,
      cidrBlock: '10.0.2.0/24',
      mapPublicIpOnLaunch: false,
      availabilityZone: cdk.Fn.select(0, cdk.Fn.getAzs('')),
      tags: [
        {
          key: 'Name',
          value: 'taskapp-PrivateSubnet',
        },
      ],
    });

    const privateSubnet2 = new ec2.CfnSubnet(this, 'PrivateSubnet2', {
      vpcId: vpc.ref,
      cidrBlock: '10.0.3.0/24',
      mapPublicIpOnLaunch: false,
      availabilityZone: cdk.Fn.select(1, cdk.Fn.getAzs('')),
      tags: [
        {
          key: 'Name',
          value: 'taskapp-PrivateSubnet2',
        },
      ],
    });

    const publicRouteTable = new ec2.CfnRouteTable(this, 'PublicRouteTable', {
      vpcId: vpc.ref,
      tags: [
        {
          key: 'Name',
          value: 'taskapp-PublicRouteTable',
        },
      ],
    });

    const publicSubnet = new ec2.CfnSubnet(this, 'PublicSubnet', {
      vpcId: vpc.ref,
      cidrBlock: '10.0.1.0/24',
      mapPublicIpOnLaunch: true,
      availabilityZone: cdk.Fn.select(0, cdk.Fn.getAzs('')),
      tags: [
        {
          key: 'Name',
          value: 'taskapp-PublicSubnet',
        },
      ],
    });

    const publicSubnet2 = new ec2.CfnSubnet(this, 'PublicSubnet2', {
      vpcId: vpc.ref,
      cidrBlock: '10.0.4.0/24',
      mapPublicIpOnLaunch: true,
      availabilityZone: cdk.Fn.select(1, cdk.Fn.getAzs('')),
      tags: [
        {
          key: 'Name',
          value: 'taskapp-PublicSubnet2',
        },
      ],
    });

    const rdsSecurityGroup = new ec2.CfnSecurityGroup(this, 'RDSSecurityGroup', {
      groupDescription: 'RDS Security Group',
      vpcId: vpc.ref,
    });

    const alb = new elasticloadbalancingv2.CfnLoadBalancer(this, 'ALB', {
      name: 'taskapp-alb',
      subnets: [
        publicSubnet.ref,
        publicSubnet2.ref,
      ],
      securityGroups: [
        albSecurityGroup.ref,
      ],
      scheme: 'internet-facing',
      type: 'application',
    });

    const ecsLaunchTemplate = new ec2.CfnLaunchTemplate(this, 'ECSLaunchTemplate', {
      launchTemplateName: `${this.stackName}-ecs-lt`,
      launchTemplateData: {
        instanceType: 't4g.small',
        imageId: `{{resolve:ssm:/aws/service/ecs/optimized-ami/amazon-linux-2023/arm64/recommended/image_id}}`,
        iamInstanceProfile: {
          name: instanceProfile.ref,
        },
        securityGroupIds: [
          ecsSecurityGroup.ref,
        ],
        userData: cdk.Fn.base64(`#!/bin/bash
        echo ECS_CLUSTER=${this.stackName}-Cluster >> /etc/ecs/ecs.config
        `),
        metadataOptions: {
          httpEndpoint: 'enabled',
          httpTokens: 'required',
        },
      },
    });

    const ecsTaskSecurityGroup = new ec2.CfnSecurityGroup(this, 'ECSTaskSecurityGroup', {
      groupDescription: 'Allow HTTP traffic to ECS tasks from ALB and egress rules',
      vpcId: vpc.ref,
      securityGroupIngress: [
        {
          ipProtocol: 'tcp',
          fromPort: 8080,
          toPort: 8080,
          sourceSecurityGroupId: albSecurityGroup.ref,
        },
      ],
      securityGroupEgress: [
        {
          ipProtocol: 'tcp',
          fromPort: 3306,
          toPort: 3306,
          destinationSecurityGroupId: rdsSecurityGroup.attrGroupId,
        },
        {
          ipProtocol: 'tcp',
          fromPort: 443,
          toPort: 443,
          cidrIp: '0.0.0.0/0',
        },
        {
          description: 'Allow outbound for DNS resolution',
          ipProtocol: 'tcp',
          fromPort: 53,
          toPort: 53,
          cidrIp: '0.0.0.0/0',
        },
        {
          description: 'Allow outbound for DNS resolution (UDP)',
          ipProtocol: 'udp',
          fromPort: 53,
          toPort: 53,
          cidrIp: '0.0.0.0/0',
        },
      ],
    });

  const fckNatSecurityGroup = new ec2.CfnSecurityGroup(this, 'FckNatSecurityGroup', {
    vpcId: vpc.ref,
    groupDescription: 'Allow traffic from private subnets to FckNat instance',
    securityGroupIngress: [
      {
        ipProtocol: "-1", // must be a string
        fromPort: -1,
        toPort: -1,
        sourceSecurityGroupId: ecsSecurityGroup.ref,
      },
    ],
    securityGroupEgress: [
      {
        ipProtocol: "-1", // must be a string
        fromPort: -1,
        toPort: -1,
        cidrIp: "0.0.0.0/0",
      },
    ],
  });

    const privateSubnet2RouteTableAssociation = new ec2.CfnSubnetRouteTableAssociation(this, 'PrivateSubnet2RouteTableAssociation', {
      subnetId: privateSubnet2.ref,
      routeTableId: privateRouteTable.ref,
    });

    const privateSubnetRouteTableAssociation = new ec2.CfnSubnetRouteTableAssociation(this, 'PrivateSubnetRouteTableAssociation', {
      subnetId: privateSubnet.ref,
      routeTableId: privateRouteTable.ref,
    });

    const publicRoute = new ec2.CfnRoute(this, 'PublicRoute', {
      routeTableId: publicRouteTable.ref,
      destinationCidrBlock: '0.0.0.0/0',
      gatewayId: internetGateway.ref,
    });
    publicRoute.addDependency(attachGateway);

    const publicSubnet2RouteTableAssociation = new ec2.CfnSubnetRouteTableAssociation(this, 'PublicSubnet2RouteTableAssociation', {
      subnetId: publicSubnet2.ref,
      routeTableId: publicRouteTable.ref,
    });

    const publicSubnetRouteTableAssociation = new ec2.CfnSubnetRouteTableAssociation(this, 'PublicSubnetRouteTableAssociation', {
      subnetId: publicSubnet.ref,
      routeTableId: publicRouteTable.ref,
    });

    const rdsSubnetGroup = new rds.CfnDBSubnetGroup(this, 'RDSSubnetGroup', {
      dbSubnetGroupDescription: 'Subnet group for RDS instance',
      subnetIds: [
        privateSubnet.ref,
        privateSubnet2.ref,
      ],
    });

    const albListener = new elasticloadbalancingv2.CfnListener(this, 'ALBListener', {
      defaultActions: [
        {
          type: 'forward',
          targetGroupArn: ecsTargetGroup.ref,
        },
      ],
      loadBalancerArn: alb.ref,
      port: 80,
      protocol: 'HTTP',
    });

    const cloudFrontDistribution = new cloudfront.CfnDistribution(this, 'CloudFrontDistribution', {
      distributionConfig: {
        aliases: [
          'taskapp.conordev.com',
        ],
        origins: [
          {
            domainName: frontendBucket.attrRegionalDomainName,
            id: 'S3Origin',
            originAccessControlId: cloudFrontOac.ref,
            s3OriginConfig: {
            },
          },
          {
            domainName: alb.attrDnsName,
            id: 'ALBOrigin',
            customOriginConfig: {
              httpPort: 80,
              httpsPort: 443,
              originProtocolPolicy: 'http-only',
            },
          },
        ],
        enabled: true,
        defaultRootObject: 'index.html',
        defaultCacheBehavior: {
          targetOriginId: 'S3Origin',
          viewerProtocolPolicy: 'redirect-to-https',
          allowedMethods: [
            'GET',
            'HEAD',
          ],
          cachePolicyId: '658327ea-f89d-4fab-a63d-7e88639e58f6',
          responseHeadersPolicyId: '67f7725c-6f97-4210-82d7-5512b31e9d03',
        },
        cacheBehaviors: [
          {
            pathPattern: '/api/*',
            targetOriginId: 'ALBOrigin',
            viewerProtocolPolicy: 'redirect-to-https',
            allowedMethods: [
              'GET',
              'HEAD',
              'OPTIONS',
              'PUT',
              'POST',
              'PATCH',
              'DELETE',
            ],
            cachePolicyId: '4135ea2d-6df8-44a3-9df3-4b5a84be39ad',
            originRequestPolicyId: '216adef6-5c7f-47e4-b989-5492eafa07d3',
          },
        ],
        customErrorResponses: [
          {
            errorCachingMinTtl: 300,
            errorCode: 403,
            responseCode: 200,
            responsePagePath: '/index.html',
          },
          {
            errorCachingMinTtl: 300,
            errorCode: 404,
            responseCode: 200,
            responsePagePath: '/index.html',
          },
        ],
        httpVersion: 'http2',
        viewerCertificate: {
          acmCertificateArn: `arn:aws:acm:us-east-1:${this.account}:certificate/971d66b8-bcab-4c82-9409-56ed95dc3d0c`,
          sslSupportMethod: 'sni-only',
          minimumProtocolVersion: 'TLSv1.2_2021',
        },
      },
    });

    const ecsAutoScalingGroup = new autoscaling.CfnAutoScalingGroup(this, 'ECSAutoScalingGroup', {
      launchTemplate: {
        launchTemplateId: ecsLaunchTemplate.ref,
        version: ecsLaunchTemplate.attrLatestVersionNumber,
      },
      vpcZoneIdentifier: [
        privateSubnet.ref,
        privateSubnet2.ref,
      ],
      minSize: "1",
      maxSize: "2",
      desiredCapacity: "1",
    });
    ecsAutoScalingGroup.cfnOptions.updatePolicy = {
      autoScalingRollingUpdate: {
        minActiveInstancesPercent: 1,
        maxBatchSize: 1,
        pauseTime: 'PT2M',
      },
    };
    const ecsTaskToRdsEgressRule = new ec2.CfnSecurityGroupEgress(this, 'ECSTaskToRDSEgressRule', {
      groupId: ecsTaskSecurityGroup.attrGroupId,
      ipProtocol: 'tcp',
      fromPort: 3306,
      toPort: 3306,
      destinationSecurityGroupId: rdsSecurityGroup.attrGroupId,
    });

    const fckNatInstance = new ec2.CfnInstance(this, 'FckNatInstance', {
      instanceType: 't4g.micro',
      sourceDestCheck: false,
      imageId: 'ami-0f9f15428c9ec17db',
      subnetId: publicSubnet.ref,
      tags: [
        {
          key: 'Name',
          value: `${this.stackName}-FckNatInstance`,
        },
      ],
      securityGroupIds: [
        fckNatSecurityGroup.ref,
      ],
      iamInstanceProfile: fckNatInstanceProfile.ref,
    });

    const rdsInstance = new rds.CfnDBInstance(this, 'RDSInstance', {
      dbInstanceIdentifier: `${this.stackName}-springboot-db`,
      allocatedStorage: "20",
      dbInstanceClass: 'db.t3.micro',
      engine: 'mysql',
      engineVersion: '8.4.4',
      masterUsername: `{{resolve:secretsmanager:/taskapp/db-credentials:SecretString:username}}`,
      masterUserPassword: `{{resolve:secretsmanager:/taskapp/db-credentials:SecretString:password}}`,
      vpcSecurityGroups: [
        rdsSecurityGroup.ref,
      ],
      dbSubnetGroupName: rdsSubnetGroup.ref,
      dbName: 'tasks',
      publiclyAccessible: false,
      multiAz: false,
    });

    const rdsToEcsTaskIngressRule = new ec2.CfnSecurityGroupIngress(this, 'RDSToECSTaskIngressRule', {
      groupId: rdsSecurityGroup.attrGroupId,
      ipProtocol: 'tcp',
      fromPort: 3306,
      toPort: 3306,
      sourceSecurityGroupId: ecsTaskSecurityGroup.attrGroupId,
    });

    const ecsTaskDefinition = new ecs.CfnTaskDefinition(this, 'ECSTaskDefinition', {
      family: 'taskapp-task-def',
      requiresCompatibilities: [
        'EC2',
      ],
      cpu: '256',
      memory: '512',
      networkMode: 'awsvpc',
      executionRoleArn: taskExecutionRole.ref,
      containerDefinitions: [
        {
          name: 'taskapp-container',
          image: `${this.account}.dkr.ecr.eu-west-1.amazonaws.com/taskapp:latest`,
          portMappings: [
            {
              containerPort: 8080,
            },
          ],
          logConfiguration: {
            logDriver: 'awslogs',
            options: {
              'awslogs-group': ecsLogGroup.ref,
              'awslogs-region': this.region,
              'awslogs-stream-prefix': 'taskapp',
            },
          },
          environment: [
            {
              name: 'SPRING_DATASOURCE_URL',
              value: `jdbc:mysql://${rdsInstance.attrEndpointAddress}:3306/tasks?allowPublicKeyRetrieval=true&useSSL=false`,
            },
            {
              name: 'SERVER_ADDRESS',
              value: '0.0.0.0',
            },
            {
              name: 'LAST_UPDATE_TRIGGER',
              value: '2025-06-03-001',
            },
            {
              name: 'SPRING_JPA_OPEN_IN_VIEW',
              value: 'false',
            },
            {
              name: 'SPRING_JPA_HIBERNATE_DDL_AUTO',
              value: 'update',
            },
          ],
          secrets: [
            {
              name: 'SPRING_DATASOURCE_USERNAME',
              valueFrom: `arn:aws:secretsmanager:${this.region}:${this.account}:secret:/taskapp/db-credentials:username::`,
            },
            {
              name: 'SPRING_DATASOURCE_PASSWORD',
              valueFrom: `arn:aws:secretsmanager:${this.region}:${this.account}:secret:/taskapp/db-credentials:password::`,
            },
            {
              name: 'JWT_SECRET',
              valueFrom: `arn:aws:secretsmanager:${this.region}:${this.account}:secret:/taskapp/db-credentials:jwt::`,
            },
          ],
        },
      ],
    });

    const frontendBucketPolicy = new s3.CfnBucketPolicy(this, 'FrontendBucketPolicy', {
      bucket: frontendBucket.ref,
      policyDocument: {
        Version: '2012-10-17',
        Statement: [
          {
            Effect: 'Allow',
            Principal: {
              Service: 'cloudfront.amazonaws.com',
            },
            Action: 's3:GetObject',
            Resource: `arn:aws:s3:::${frontendBucket.ref}/*`,
            Condition: {
              StringEquals: {
                'AWS:SourceArn': `arn:aws:cloudfront::${this.account}:distribution/${cloudFrontDistribution.ref}`,
              },
            },
          },
        ],
      },
    });

    const gitHubActionsRole = new iam.CfnRole(this, 'GitHubActionsRole', {
      roleName: 'GitHub_Actions_Role',
      assumeRolePolicyDocument: {
        Version: '2012-10-17',
        Statement: [
          {
            Effect: 'Allow',
            Principal: {
              Federated: `arn:aws:iam::${this.account}:oidc-provider/token.actions.githubusercontent.com`,
            },
            Action: 'sts:AssumeRoleWithWebIdentity',
            Condition: {
              StringLike: {
                'token.actions.githubusercontent.com:sub': [
                  props.gitHubRepoSubClaim!,
                ],
              },
              StringEqualsIgnoreCase: {
                'token.actions.githubusercontent.com:aud': 'sts.amazonaws.com',
              },
            },
          },
        ],
      },
      policies: [
        {
          policyName: 'OidcSafetyPolicy',
          policyDocument: {
            Version: '2012-10-17',
            Statement: [
              {
                Sid: 'OidcSafeties',
                Effect: 'Deny',
                Action: [
                  'sts:AssumeRole',
                ],
                Resource: '*',
              },
            ],
          },
        },
        {
          policyName: 'GitHubActionsDeployPolicy',
          policyDocument: {
            Version: '2012-10-17',
            Statement: [
              {
                Sid: 'AllowS3Actions',
                Effect: 'Allow',
                Action: [
                  's3:PutObject',
                ],
                Resource: [
                  frontendBucket.attrArn,
                  `${frontendBucket.attrArn}/*`,
                ],
              },
              {
                Sid: 'AllowCloudFrontInvalidation',
                Effect: 'Allow',
                Action: [
                  'cloudfront:CreateInvalidation',
                ],
                Resource: [
                  `arn:aws:cloudfront::${this.account}:distribution/${cloudFrontDistribution.ref}`,
                ],
              },
              {
                Sid: 'AllowEcrLogin',
                Effect: 'Allow',
                Action: [
                  'ecr:GetAuthorizationToken',
                ],
                Resource: '*',
              },
              {
                Sid: 'AllowEcsAndEcrDeployments',
                Effect: 'Allow',
                Action: [
                  'ecs:UpdateService',
                  'ecs:DescribeServices',
                  'ecr:BatchCheckLayerAvailability',
                  'ecr:InitiateLayerUpload',
                  'ecr:UploadLayerPart',
                  'ecr:CompleteLayerUpload',
                  'ecr:PutImage',
                ],
                Resource: [
                  `arn:aws:ecs:${this.region}:${this.account}:cluster/${ecsCluster.ref}`,
                  `arn:aws:ecs:${this.region}:${this.account}:service/${ecsCluster.ref}/*`,
                  `arn:aws:ecr:${this.region}:${this.account}:repository/taskapp`,
                ],
              },
            ],
          },
        },
      ],
    });

    const privateRouteTableRoute = new ec2.CfnRoute(this, 'PrivateRouteTableRoute', {
      routeTableId: privateRouteTable.ref,
      destinationCidrBlock: '0.0.0.0/0',
      instanceId: fckNatInstance.ref,
    });

    const ecsService = new ecs.CfnService(this, 'ECSService', {
      cluster: ecsCluster.ref,
      desiredCount: 1,
      launchType: 'EC2',
      taskDefinition: ecsTaskDefinition.ref,
      healthCheckGracePeriodSeconds: 240,
      networkConfiguration: {
        awsvpcConfiguration: {
          assignPublicIp: 'DISABLED',
          securityGroups: [
            ecsTaskSecurityGroup.ref,
          ],
          subnets: [
            privateSubnet.ref,
            privateSubnet2.ref,
          ],
        },
      },
      loadBalancers: [
        {
          containerName: 'taskapp-container',
          containerPort: 8080,
          targetGroupArn: ecsTargetGroup.ref,
        },
      ],
    });
    ecsService.addDependency(ecsCluster);
    ecsService.addDependency(ecsAutoScalingGroup);
    ecsService.addDependency(ecsLogGroup);
    ecsService.addDependency(ecsTaskDefinition);
    ecsService.addDependency(albListener);

    // Outputs
    this.frontendtaskapp = `https://${cloudFrontDistribution.attrDomainName}`;
    new cdk.CfnOutput(this, 'CfnOutputFrontendtaskapp', {
      key: 'Frontendtaskapp',
      description: 'React app task-app (via CloudFront HTTPS)',
      exportName: `${this.stackName}-Frontendtaskapp`,
      value: this.frontendtaskapp!.toString(),
    });
    this.rdsEndpoint = rdsInstance.attrEndpointAddress;
    new cdk.CfnOutput(this, 'CfnOutputRDSEndpoint', {
      key: 'RDSEndpoint',
      description: 'RDS endpoint for backend app',
      exportName: `${this.stackName}-RDSEndpoint`,
      value: this.rdsEndpoint!.toString(),
    });
    this.albEndpoint = alb.attrDnsName;
    new cdk.CfnOutput(this, 'CfnOutputALBEndpoint', {
      key: 'ALBEndpoint',
      description: 'ALB endpoint for backend app',
      exportName: `${this.stackName}-ALBEndpoint`,
      value: this.albEndpoint!.toString(),
    });
  }
}
