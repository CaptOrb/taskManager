export interface Task {
	id: number;
	title: string;
	description: string;
	status: "TODO" | "IN_PROGRESS" | "COMPLETED";
	priority: "LOW" | "MEDIUM" | "HIGH";
	createdDate: string;
	dueDate: string;
	completedDate?: string;
	userId: number;
}
