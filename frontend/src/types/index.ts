// Common TypeScript interfaces and types

export interface Task {
  id: number;
  title: string;
  description: string;
  status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  createdDate: string;
  dueDate: string;
  completedDate?: string;
  userId: number;
}

export interface User {
  id: number;
  userName: string;
  email: string;
  // Add other user properties as needed
}

export interface LoginRequest {
  userName: string;
  password: string;
}

export interface LoginResponse {
  jwtToken: string;
}

export interface RegisterRequest {
  userName: string;
  email: string;
  password: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ApiError {
  message: string;
  status: number;
}
