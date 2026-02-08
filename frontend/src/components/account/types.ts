import type { ChangeEvent, FormEvent, ReactNode } from "react";

export type PasswordField =
	| "currentPassword"
	| "newPassword"
	| "confirmPassword";

export type PasswordForm = Record<PasswordField, string>;

export type PasswordFieldErrors = Record<PasswordField, string[]>;

export type NotificationSettingsResponse = {
	enabled?: boolean;
	publicUrl?: string;
	topicPrefix?: string;
	topic?: string;
	reminderMinutesBeforeDue?: number;
};

export type NotificationTopicSuggestionResponse = {
	topic?: string;
};

export type NotificationForm = {
	enabled: boolean;
	topic: string;
};

export type AccountInfoSectionProps = {
	userName: string;
	email: string;
	userNameId: string;
	emailId: string;
	children?: ReactNode;
};

export type PasswordSectionProps = {
	showPasswordForm: boolean;
	passwordChangeSuccess: string | null;
	passwordChangeError: string | null;
	currentPasswordId: string;
	newPasswordId: string;
	confirmPasswordId: string;
	passwordForm: PasswordForm;
	passwordFieldErrors: PasswordFieldErrors;
	isChangingPassword: boolean;
	onOpenPasswordForm: () => void;
	onPasswordChange: (e: FormEvent<HTMLFormElement>) => Promise<void>;
	onPasswordInputChange: (e: ChangeEvent<HTMLInputElement>) => void;
	onCancelPasswordChange: () => void;
};

export type NotificationSettingsSectionProps = {
	notificationError: string | null;
	notificationSuccess: string | null;
	notificationLoading: boolean;
	notificationSaving: boolean;
	notificationTesting: boolean;
	topicSuggestionLoading: boolean;
	notificationForm: NotificationForm;
	reminderMinutesBeforeDue: number;
	publicNtfyBaseUrl: string;
	ntfyTopicPreview: string;
	ntfyTopicPrefixDisplay: string;
	ntfySubscribeUrl: string | null;
	copySubscribeUrlFeedback: string | null;
	ntfyTopicId: string;
	onNotificationInputChange: (e: ChangeEvent<HTMLInputElement>) => void;
	onNotificationSettingsSave: (e: FormEvent<HTMLFormElement>) => Promise<void>;
	onSendTestNotification: () => Promise<void>;
	onCopySubscribeUrl: () => Promise<void>;
	onGenerateTopic: () => void;
};
