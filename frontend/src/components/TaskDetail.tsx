import axios, { AxiosError } from "axios";
import type { ReactElement } from "react";
import { useEffect, useId, useState } from "react";
import ReactMarkdown from "react-markdown";
import { Link, useNavigate, useParams } from "react-router-dom";
import type { Task } from "@/types/task";
import { useAuth } from "../hooks/auth-context";

const TaskDetail = (): ReactElement => {
	const { id } = useParams();
	const [task, setTask] = useState<Task | null>(null);
	const [loading, setLoading] = useState<boolean>(true);
	const [error, setError] = useState<string>("");
	const [isEditing, setIsEditing] = useState(false);
	const { loggedInUser } = useAuth();
	const navigate = useNavigate();

	const [taskTitle, setTaskTitle] = useState("");
	const [taskDescription, setTaskDescription] = useState("");
	const [taskStatus, setTaskStatus] = useState("");
	const [taskPriority, setTaskPriority] = useState("");
	const [taskDueDate, setTaskDueDate] = useState("");
	const [showPreview, setShowPreview] = useState(false);

	const taskTitleId = useId();
	const taskDescriptionId = useId();
	const taskStatusId = useId();
	const taskPriorityId = useId();
	const taskDueDateId = useId();

	useEffect(() => {
		const fetchTask = async (): Promise<void> => {
			if (loggedInUser) {
				try {
					setLoading(true);
					const response = await axios.get(`/api/tasks/${id}`, {
						headers: {
							Authorization: `Bearer ${localStorage.getItem("token")}`,
						},
					});
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
						setError(
							`Error fetching task: ${error instanceof AxiosError ? error.response?.data || error.message : "Unknown error"}`,
						);
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
		const updatedTask = {
			title: taskTitle,
			description: taskDescription,
			status: taskStatus,
			priority: taskPriority,
			dueDate: new Date(taskDueDate).toISOString(),
		};

		try {
			const response = await axios.put(`/api/tasks/${id}`, updatedTask, {
				headers: {
					Authorization: `Bearer ${localStorage.getItem("token")}`,
				},
			});

			if (response.status === 200) {
				setTask((prevTask) => ({
					...prevTask,
					...response.data,
				}));
				setError("");
				setIsEditing(false);
			}
		} catch (error) {
			setError(
				`Error updating task: ${error instanceof AxiosError ? error.response?.data || error.message : "Unknown error"}`,
			);
		}
	};

	const handleDelete = async (): Promise<void> => {
		const confirmDelete = window.confirm(
			"Are you sure you want to delete this task?",
		);
		if (confirmDelete) {
			try {
				await axios.delete(`/api/tasks/delete/${id}`, {
					headers: {
						Authorization: `Bearer ${localStorage.getItem("token")}`,
					},
				});
				navigate("/");
			} catch (error) {
				setError(
					`Error deleting task: ${error instanceof AxiosError ? error.response?.data || error.message : "Unknown error"}`,
				);
			}
		}
	};

	if (loading) return <div>Loading task...</div>;
	if (error && !isEditing) return <div style={{ color: "red" }}>{error}</div>;
	if (!task) return <div>No task found.</div>;

	return (
		<div className="max-w-xl md:max-w-2xl lg:max-w-4xl xl:max-w-5xl mx-auto p-6 bg-white shadow-lg rounded-lg dark:bg-gray-800">
			<Link to="/">
				<button
					type="button"
					className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-4 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800"
				>
					Back
				</button>
			</Link>

			{isEditing ? (
				<div>
					<h2 className="text-2xl font-bold mb-6 text-gray-900 dark:text-white">
						Edit Task
					</h2>

					{error && <p className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg dark:bg-red-900 dark:border-red-700 dark:text-red-200">{error}</p>}

					<div className="mb-6">
						<label
							htmlFor={taskTitleId}
							className="block text-sm font-medium text-gray-700 dark:text-white mb-2"
						>
							Title
						</label>
						<input
							id={taskTitleId}
							type="text"
							value={taskTitle}
							onChange={(e) => setTaskTitle(e.target.value)}
							className="bg-gray-50 border border-gray-300 text-sm rounded-lg w-full p-3 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
						/>
					</div>

					<div className="mb-6">
						<div className="flex items-center justify-between mb-2">
							<div className="flex items-center gap-2">
								<label
									htmlFor={taskDescriptionId}
									className="block text-sm font-medium text-gray-700 dark:text-white"
								>
									Description
								</label>
							</div>
							<button
								type="button"
								onClick={() => setShowPreview(!showPreview)}
								className="text-sm text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 font-medium"
							>
								{showPreview ? 'Hide Preview' : 'Show Preview'}
							</button>
						</div>
						<p className="text-xs text-gray-500 dark:text-gray-400 mb-2">
							Supports Markdown formatting (e.g., **bold**, *italic*, [links](url), # headings)
						</p>
						<div className={showPreview ? "grid grid-cols-1 lg:grid-cols-2 gap-4" : ""}>
							<div className={showPreview ? "" : "w-full"}>
								<textarea
									id={taskDescriptionId}
									value={taskDescription}
									onChange={(e) => setTaskDescription(e.target.value)}
									className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-4 dark:bg-gray-700 dark:border-gray-600 dark:text-white resize-none font-mono"
									wrap="hard"
									rows={12}
								/>
							</div>
							{showPreview && (
								<div>
									<p className="block text-sm font-medium text-gray-700 dark:text-white mb-2">
										Preview:
									</p>
									<div className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-4 dark:bg-gray-700 dark:border-gray-600 dark:text-white break-words overflow-y-auto min-h-[300px]">
										<div className="markdown text-gray-700 dark:text-gray-300 leading-relaxed">
											<ReactMarkdown>{taskDescription || "*No preview available*"}</ReactMarkdown>
										</div>
									</div>
								</div>
							)}
						</div>
					</div>

					<div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
						<div>
							<label
								htmlFor={taskStatusId}
								className="block text-sm font-medium text-gray-700 dark:text-white mb-2"
							>
								Status
							</label>
							<select
								id={taskStatusId}
								value={taskStatus}
								onChange={(e) => setTaskStatus(e.target.value)}
								className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-3 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
							>
								<option value="PENDING">Pending</option>
								<option value="IN_PROGRESS">In Progress</option>
								<option value="COMPLETED">Completed</option>
							</select>
						</div>

						<div>
							<label
								htmlFor={taskPriorityId}
								className="block text-sm font-medium text-gray-700 dark:text-white mb-2"
							>
								Priority
							</label>
							<select
								id={taskPriorityId}
								value={taskPriority}
								onChange={(e) => setTaskPriority(e.target.value)}
								className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-3 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
							>
								<option value="LOW">Low</option>
								<option value="MEDIUM">Medium</option>
								<option value="HIGH">High</option>
							</select>
						</div>

						<div>
							<label
								htmlFor={taskDueDateId}
								className="block text-sm font-medium text-gray-700 dark:text-white mb-2"
							>
								Due Date
							</label>
							<input
								id={taskDueDateId}
								type="datetime-local"
								value={taskDueDate}
								onChange={(e) => setTaskDueDate(e.target.value)}
								className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-3 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
							/>
						</div>
					</div>

					<div className="flex gap-4">
						<button
							type="button"
							onClick={handleUpdate}
							className="text-white bg-green-700 hover:bg-green-800 font-medium rounded-lg text-sm px-6 py-3"
						>
							Save Changes
						</button>

						<button
							type="button"
							onClick={() => setIsEditing(false)}
							className="text-white bg-gray-600 hover:bg-gray-700 font-medium rounded-lg text-sm px-6 py-3"
						>
							Cancel
						</button>
					</div>
				</div>
			) : (
				<div className="mt-4">
					{/* Header Section */}
					<div className="mb-6 pb-6 border-b border-gray-200 dark:border-gray-700">
						<div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
							<h1 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 dark:text-white">
								{task.title}
							</h1>
							<div className="flex gap-2 flex-shrink-0">
								<span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${task.status === 'COMPLETED' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
										task.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' :
											'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
									}`}>
									{task.status.replace(/_/g, ' ')}
								</span>
								<span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${task.priority === 'HIGH' ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' :
										task.priority === 'MEDIUM' ? 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200' :
											'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'
									}`}>
									{task.priority}
								</span>
							</div>
						</div>
					</div>

					<div className="mb-4">
						<div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl shadow-sm px-8 py-6">
							<div className="prose prose-gray dark:prose-invert max-w-none">
								<div className="markdown text-gray-700 dark:text-gray-300 leading-relaxed">
									<ReactMarkdown>{task.description || "*No description provided*"}</ReactMarkdown>
								</div>
							</div>
						</div>
					</div>

					{/* Dates and Actions Row */}
					<div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8 pb-6 border-b border-gray-200 dark:border-gray-700">
						<div className="grid grid-cols-1 sm:grid-cols-2 gap-6 flex-1">
							<div>
								<p className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">
									Created
								</p>
								<p className="text-sm text-gray-700 dark:text-gray-300">
									{new Date(task.createdDate).toLocaleDateString(navigator.language, {
										year: 'numeric',
										month: 'long',
										day: 'numeric',
										hour: '2-digit',
										minute: '2-digit'
									})}
								</p>
							</div>

							<div>
								<p className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">
									Due Date
								</p>
								<p className="text-sm text-gray-700 dark:text-gray-300">
									{new Date(task.dueDate).toLocaleDateString(navigator.language, {
										year: 'numeric',
										month: 'long',
										day: 'numeric',
										hour: '2-digit',
										minute: '2-digit'
									})}
								</p>
							</div>
						</div>
					</div>

					{/* Action Buttons */}
					<div className="flex flex-col sm:flex-row gap-4">
						<button
							type="button"
							onClick={() => setIsEditing(true)}
							className="flex-1 bg-yellow-500 hover:bg-yellow-600 text-white font-medium rounded-lg px-6 py-3 transition-colors shadow-sm hover:shadow-md"
						>
							Edit Task
						</button>

						<button
							type="button"
							onClick={handleDelete}
							className="flex-1 bg-red-600 hover:bg-red-700 text-white font-medium rounded-lg px-6 py-3 transition-colors shadow-sm hover:shadow-md"
						>
							Delete Task
						</button>
					</div>
				</div>
			)}
		</div>
	);
};

export default TaskDetail;
