import { AxiosError } from "axios";
import type { ReactElement } from "react";
import { useEffect, useId, useState } from "react";
import ReactMarkdown from "react-markdown";
import { Link, useNavigate, useParams } from "react-router-dom";
import type { Task } from "@/types/task";
import { useAuth } from "../hooks/auth-context";
import api from "../utils/api";
import { getApiErrorMessage, getApiFieldErrors } from "../utils/apiError";

type TaskField = "title" | "description" | "status" | "priority" | "dueDate";

type TaskFieldErrors = Record<TaskField, string[]>;

const createEmptyFieldErrors = (): TaskFieldErrors => ({
	title: [],
	description: [],
	status: [],
	priority: [],
	dueDate: [],
});

const mapApiFieldErrorsToTaskFields = (
	apiFieldErrors: Record<string, string[]>,
): TaskFieldErrors => ({
	title: apiFieldErrors["title"] ?? [],
	description: apiFieldErrors["description"] ?? [],
	status: apiFieldErrors["status"] ?? [],
	priority: apiFieldErrors["priority"] ?? [],
	dueDate: apiFieldErrors["dueDate"] ?? [],
});

const hasAnyFieldError = (fieldErrors: TaskFieldErrors): boolean =>
	Object.values(fieldErrors).some(
		(errorsForField) => errorsForField.length > 0,
	);

const getInputClassName = (hasError: boolean): string =>
	`bg-gray-50 border text-gray-900 text-sm rounded-lg w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:text-white ${hasError ? "border-red-500 focus:ring-red-500 focus:border-red-500 dark:focus:ring-red-500 dark:focus:border-red-500" : "border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:focus:ring-blue-500 dark:focus:border-blue-500"}`;

const TaskDetail = (): ReactElement => {
	const { id } = useParams();
	const [task, setTask] = useState<Task | null>(null);
	const [loading, setLoading] = useState<boolean>(true);
	const [error, setError] = useState<string>("");
	const [isEditing, setIsEditing] = useState(false);
	const [fieldErrors, setFieldErrors] = useState<TaskFieldErrors>(() =>
		createEmptyFieldErrors(),
	);
	const { loggedInUser } = useAuth();
	const navigate = useNavigate();

	const [taskTitle, setTaskTitle] = useState("");
	const [taskDescription, setTaskDescription] = useState("");
	const [taskStatus, setTaskStatus] = useState("");
	const [taskPriority, setTaskPriority] = useState("");
	const [taskDueDate, setTaskDueDate] = useState("");

	const taskTitleId = useId();
	const taskDescriptionId = useId();
	const taskStatusId = useId();
	const taskPriorityId = useId();
	const taskDueDateId = useId();

	const clearFieldError = (field: TaskField): void => {
		setFieldErrors((previousErrors) => ({
			...previousErrors,
			[field]: [],
		}));
		setError("");
	};

	useEffect(() => {
		const fetchTask = async (): Promise<void> => {
			if (loggedInUser) {
				try {
					setLoading(true);
					const response = await api.get<Task>(`/tasks/${id}`);
					const fetchedTask = response.data;
					setTask(fetchedTask);
					setTaskTitle(fetchedTask.title);
					setTaskDescription(fetchedTask.description);
					setTaskStatus(fetchedTask.status);
					setTaskPriority(fetchedTask.priority);
					setTaskDueDate(
						new Date(fetchedTask.dueDate).toISOString().slice(0, 16),
					);
				} catch (error) {
					if (
						error instanceof AxiosError &&
						(error.response?.status === 404 || error.response?.status === 403)
					) {
						navigate("/");
					} else {
						const errorMessage = getApiErrorMessage(error);
						setError(`Error fetching task: ${errorMessage}`);
					}
				} finally {
					setLoading(false);
				}
			} else {
				setLoading(false);
			}
		};

		fetchTask();
	}, [loggedInUser, id, navigate]);

	const handleUpdate = async (): Promise<void> => {
		setError("");
		setFieldErrors(createEmptyFieldErrors());

		const updatedTask = {
			title: taskTitle,
			description: taskDescription,
			status: taskStatus,
			priority: taskPriority,
			dueDate: new Date(taskDueDate).toISOString(),
		};

		try {
			const response = await api.put<Task>(`/tasks/${id}`, updatedTask);

			if (response.status === 200) {
				setTask((prevTask) => ({
					...prevTask,
					...response.data,
				}));
				setError("");
				setFieldErrors(createEmptyFieldErrors());
				setIsEditing(false);
			}
		} catch (error) {
			const fieldLevelErrors = mapApiFieldErrorsToTaskFields(
				getApiFieldErrors(error),
			);
			setFieldErrors(fieldLevelErrors);

			if (!hasAnyFieldError(fieldLevelErrors)) {
				const errorMessage = getApiErrorMessage(error);
				setError(`Error updating task: ${errorMessage}`);
			}
		}
	};

	const handleDelete = async (): Promise<void> => {
		const confirmDelete = window.confirm(
			"Are you sure you want to delete this task?",
		);
		if (confirmDelete) {
			try {
				await api.delete(`/tasks/${id}`);
				navigate("/");
			} catch (error) {
				const errorMessage = getApiErrorMessage(error);
				setError(`Error deleting task: ${errorMessage}`);
			}
		}
	};

	if (loading) return <div>Loading task...</div>;
	if (error && !isEditing) return <div style={{ color: "red" }}>{error}</div>;
	if (!task) return <div>No task found.</div>;

	return (
		<div className="max-w-xl mx-auto p-6 bg-white shadow-lg rounded-lg dark:bg-gray-800">
			<Link to="/">
				<button
					type="button"
					className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800"
				>
					Back
				</button>
			</Link>

			{isEditing ? (
				<div>
					<h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">
						Edit Task
					</h2>

					{error && <p style={{ color: "red" }}>{error}</p>}

					<div className="mb-4">
						{fieldErrors.title.map((fieldError) => (
							<p
								key={`title-${fieldError}`}
								className="text-red-500 text-sm mb-1"
							>
								{fieldError}
							</p>
						))}
						<label
							htmlFor={taskTitleId}
							className="block text-sm font-medium text-gray-700 dark:text-white"
						>
							Title
						</label>
						<input
							id={taskTitleId}
							type="text"
							value={taskTitle}
							onChange={(e) => {
								setTaskTitle(e.target.value);
								clearFieldError("title");
							}}
							aria-invalid={fieldErrors.title.length > 0}
							className={getInputClassName(fieldErrors.title.length > 0)}
						/>
					</div>

					<div className="mb-4">
						{fieldErrors.description.map((fieldError) => (
							<p
								key={`description-${fieldError}`}
								className="text-red-500 text-sm mb-1"
							>
								{fieldError}
							</p>
						))}
						<label
							htmlFor={taskDescriptionId}
							className="block text-sm font-medium text-gray-700 dark:text-white"
						>
							Description
						</label>
						<textarea
							id={taskDescriptionId}
							value={taskDescription}
							onChange={(e) => {
								setTaskDescription(e.target.value);
								clearFieldError("description");
							}}
							aria-invalid={fieldErrors.description.length > 0}
							className={getInputClassName(fieldErrors.description.length > 0)}
							wrap="hard"
						/>
					</div>

					<div className="mb-4">
						<p className="block text-sm font-medium text-gray-700 dark:text-white">
							Description Preview
						</p>
						<div className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:text-white break-words">
							<div className="markdown">
								<ReactMarkdown>{taskDescription}</ReactMarkdown>
							</div>
						</div>
					</div>

					<div className="mb-4">
						{fieldErrors.status.map((fieldError) => (
							<p
								key={`status-${fieldError}`}
								className="text-red-500 text-sm mb-1"
							>
								{fieldError}
							</p>
						))}
						<label
							htmlFor={taskStatusId}
							className="block text-sm font-medium text-gray-700 dark:text-white"
						>
							Status
						</label>
						<select
							id={taskStatusId}
							value={taskStatus}
							onChange={(e) => {
								setTaskStatus(e.target.value);
								clearFieldError("status");
							}}
							aria-invalid={fieldErrors.status.length > 0}
							className={getInputClassName(fieldErrors.status.length > 0)}
						>
							<option value="PENDING">Pending</option>
							<option value="IN_PROGRESS">In Progress</option>
							<option value="COMPLETED">Completed</option>
						</select>
					</div>

					<div className="mb-4">
						{fieldErrors.priority.map((fieldError) => (
							<p
								key={`priority-${fieldError}`}
								className="text-red-500 text-sm mb-1"
							>
								{fieldError}
							</p>
						))}
						<label
							htmlFor={taskPriorityId}
							className="block text-sm font-medium text-gray-700 dark:text-white"
						>
							Priority
						</label>
						<select
							id={taskPriorityId}
							value={taskPriority}
							onChange={(e) => {
								setTaskPriority(e.target.value);
								clearFieldError("priority");
							}}
							aria-invalid={fieldErrors.priority.length > 0}
							className={getInputClassName(fieldErrors.priority.length > 0)}
						>
							<option value="LOW">Low</option>
							<option value="MEDIUM">Medium</option>
							<option value="HIGH">High</option>
						</select>
					</div>

					<div className="mb-4">
						{fieldErrors.dueDate.map((fieldError) => (
							<p
								key={`dueDate-${fieldError}`}
								className="text-red-500 text-sm mb-1"
							>
								{fieldError}
							</p>
						))}
						<label
							htmlFor={taskDueDateId}
							className="block text-sm font-medium text-gray-700 dark:text-white"
						>
							Due Date
						</label>
						<input
							id={taskDueDateId}
							type="datetime-local"
							value={taskDueDate}
							onChange={(e) => {
								setTaskDueDate(e.target.value);
								clearFieldError("dueDate");
							}}
							aria-invalid={fieldErrors.dueDate.length > 0}
							className={getInputClassName(fieldErrors.dueDate.length > 0)}
						/>
					</div>

					<button
						type="button"
						onClick={handleUpdate}
						className="text-white bg-green-700 hover:bg-green-800 font-medium rounded-lg text-sm px-5 py-2.5 mr-2 mb-2"
					>
						Save Changes
					</button>

					<button
						type="button"
						onClick={() => {
							setError("");
							setFieldErrors(createEmptyFieldErrors());
							setIsEditing(false);
							// Reset form fields to original task values
							if (task) {
								setTaskTitle(task.title);
								setTaskDescription(task.description);
								setTaskStatus(task.status);
								setTaskPriority(task.priority);
								setTaskDueDate(
									new Date(task.dueDate).toISOString().slice(0, 16),
								);
							}
						}}
						className="text-white bg-red-700 hover:bg-red-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2"
					>
						Cancel
					</button>
				</div>
			) : (
				<div className="max-w-2xl mx-auto mt-8 p-8 bg-white shadow-lg rounded-lg dark:bg-gray-800">
					<h2 className="text-3xl font-bold mb-6 text-gray-900 dark:text-white">
						{task.title}
					</h2>

					<div className="mb-6">
						<div className="mb-4">
							<p className="text-gray-700 dark:text-gray-300 font-semibold mb-1">
								Description:
							</p>

							<div className="bg-gray-50 border border-gray-300 p-4 rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white break-words">
								<div className="markdown">
									<ReactMarkdown>{task.description}</ReactMarkdown>
								</div>
							</div>
						</div>

						<div className="grid grid-cols-2 gap-4 mb-6">
							<div>
								<p className="text-sm font-semibold text-gray-500 dark:text-gray-400">
									Status:
								</p>
								<p className="text-gray-900 dark:text-gray-300">
									{task.status}
								</p>
							</div>

							<div>
								<p className="text-sm font-semibold text-gray-500 dark:text-gray-400">
									Priority:
								</p>
								<p className="text-gray-900 dark:text-gray-300">
									{task.priority}
								</p>
							</div>

							<div>
								<p className="text-sm font-semibold text-gray-500 dark:text-gray-400">
									Created Date:
								</p>
								<p className="text-gray-900 dark:text-gray-300">
									{new Date(task.createdDate).toLocaleString()}
								</p>
							</div>

							<div>
								<p className="text-sm font-semibold text-gray-500 dark:text-gray-400">
									Due Date:
								</p>
								<p className="text-gray-900 dark:text-gray-300">
									{new Date(task.dueDate).toLocaleString()}
								</p>
							</div>
						</div>

						<div className="flex space-x-4">
							<button
								type="button"
								onClick={() => {
									setError("");
									setFieldErrors(createEmptyFieldErrors());
									setIsEditing(true);
								}}
								className="flex-1 bg-yellow-500 text-white font-medium rounded-lg text-sm px-5 py-2.5 hover:bg-yellow-600"
							>
								Edit Task
							</button>

							<button
								type="button"
								onClick={handleDelete}
								className="flex-1 bg-red-700 text-white font-medium rounded-lg text-sm px-5 py-2.5 hover:bg-red-800"
							>
								Delete Task
							</button>
						</div>
					</div>
				</div>
			)}
		</div>
	);
};

export default TaskDetail;
