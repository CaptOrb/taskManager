import axios from "axios";
import type { ReactElement } from "react";
import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/auth-context";
import type { Task } from "../types/task";

const TaskList = (): ReactElement => {
	const { loggedInUser } = useAuth();
	const navigate = useNavigate();

	const [tasks, setTasks] = useState<Task[]>([]);
	const [loading, setLoading] = useState<boolean>(true);
	const [error, setError] = useState<string>("");

	const [currentPage, setCurrentPage] = useState(1);
	const tasksPerPage = 5;

	const [statusFilter, setStatusFilter] = useState("ALL");
	const [urgencyFilter, setUrgencyFilter] = useState("ANY");
	const [sortOrder, setSortOrder] = useState("createdDate");

	useEffect(() => {
		const fetchTasks = async (): Promise<void> => {
			if (!loggedInUser) {
				setLoading(false);
				return;
			}

			try {
				setLoading(true);
				const response = await axios.get("/api/tasks", {
					headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
				});

				if (Array.isArray(response.data)) setTasks(response.data);
				else setError("Unexpected data format.");
			} catch (err: unknown) {
				const message = err instanceof Error ? err.message : String(err);
				setError(`Error fetching tasks: ${message}`);
			} finally {
				setLoading(false);
			}
		};

		fetchTasks();
	}, [loggedInUser]);

	const filteredTasks = useMemo(() => {
		const filtered = tasks.filter(
			(t) =>
				(statusFilter === "ALL" || t.status === statusFilter) &&
				(urgencyFilter === "ANY" || t.priority === urgencyFilter),
		);

		return filtered.sort((a, b) => {
			if (sortOrder === "dueDateAsc")
				return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
			if (sortOrder === "dueDateDesc")
				return new Date(b.dueDate).getTime() - new Date(a.dueDate).getTime();
			return (
				new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime()
			);
		});
	}, [tasks, statusFilter, urgencyFilter, sortOrder]);

	const indexOfLastTask = currentPage * tasksPerPage;
	const indexOfFirstTask = indexOfLastTask - tasksPerPage;
	const currentTasks = filteredTasks.slice(indexOfFirstTask, indexOfLastTask);
	const totalPages = Math.ceil(filteredTasks.length / tasksPerPage);

	//  Handlers
	const handleNavigate = (id: number): void => navigate(`/tasks/${id}`);
	const handleNextPage = (): void =>
		setCurrentPage((p) => Math.min(p + 1, totalPages));
	const handlePreviousPage = (): void =>
		setCurrentPage((p) => Math.max(p - 1, 1));

	if (!loggedInUser)
		return (
			<div className="flex items-center justify-center">
				<div className="bg-red-100 text-red-700 p-4 rounded-lg shadow-md max-w-md mx-auto">
					<p className="text-lg font-semibold text-center">
						Please log in to view tasks.
					</p>
				</div>
			</div>
		);

	if (loading) return <div>Loading tasks...</div>;
	if (error) return <div style={{ color: "red" }}>{error}</div>;
	return (
		<div className="max-w-xl mx-auto p-4 bg-white shadow-lg rounded-lg dark:bg-gray-800">
			<h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">
				Task List
			</h2>

			<div className="flex flex-col mt-4 mb-4">
				<div className="flex flex-col sm:flex-row gap-4 sm:gap-4">
					<select
						value={statusFilter}
						onChange={(e) => setStatusFilter(e.target.value)}
						className="block w-auto appearance-none border rounded p-2 bg-white text-gray-800 pl-3 pr-10"
					>
						<option value="ALL">All Statuses</option>
						<option value="PENDING">Pending</option>
						<option value="IN_PROGRESS">In Progress</option>
						<option value="COMPLETED">Completed</option>
					</select>
					<select
						value={urgencyFilter}
						onChange={(e) => setUrgencyFilter(e.target.value)}
						className="block w-auto appearance-none border rounded p-2 bg-white text-gray-800 pl-3 pr-10"
					>
						<option value="ANY">Any Priority</option>
						<option value="LOW">Low</option>
						<option value="MEDIUM">Medium</option>
						<option value="HIGH">High</option>
					</select>
					<select
						value={sortOrder}
						onChange={(e) => setSortOrder(e.target.value)}
						className="block w-auto appearance-none border rounded p-2 bg-white text-gray-800 pl-3 pr-10"
					>
						<option value="createdDate">Recently Created</option>
						<option value="dueDateAsc">Due Date: Ascending</option>
						<option value="dueDateDesc">Due Date: Descending</option>
					</select>
				</div>
				<Link
					to="/createTask"
					className="mt-4 text-white bg-blue-500 hover:bg-blue-700 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-4 lg:px-5 py-2 lg:py-2.5 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none"
				>
					Create Task
				</Link>
			</div>

			<ul className="space-y-4">
				{currentTasks.map((task) => (
					<li
						key={task.id}
						className="p-4 bg-gray-50 rounded-lg shadow-sm border border-gray-200 dark:bg-gray-700 dark:border-gray-600 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800"
						onClick={() => handleNavigate(task.id)}
						onKeyDown={(e) => {
							if (e.key === "Enter" || e.key === " ") handleNavigate(task.id);
						}}
					>
						<h3 className="text-lg font-semibold text-gray-900 dark:text-white">
							{task.title}
						</h3>
						<p className="text-sm">
							<span className="font-semibold text-gray-500 dark:text-gray-400">
								Status:{" "}
							</span>
							{task.status}
						</p>
						<p className="text-sm">
							<span className="font-semibold text-gray-500 dark:text-gray-400">
								Created Date:{" "}
							</span>
							{new Date(task.createdDate).toLocaleString()}
						</p>
						<p className="text-sm">
							<span className="font-semibold text-gray-500 dark:text-gray-400">
								Due Date:{" "}
							</span>
							{new Date(task.dueDate).toLocaleString()}
						</p>
						<p className="text-sm">
							<span className="font-semibold text-gray-500 dark:text-gray-400">
								Priority:{" "}
							</span>
							<span
								className={
									task.priority === "HIGH"
										? "text-red-500"
										: task.priority === "MEDIUM"
											? "text-yellow-500"
											: "text-green-500"
								}
							>
								{task.priority}
							</span>
						</p>
						<div className="mt-4">
							<Link
								to={`/tasks/${task.id}`}
								className="text-blue-600 dark:text-blue-400 hover:underline"
							>
								View Task
							</Link>
						</div>
					</li>
				))}
			</ul>

			{/* Pagination Controls */}
			{totalPages > 1 && (
				<div className="flex justify-between mt-4">
					<button
						type="button"
						onClick={handlePreviousPage}
						disabled={currentPage === 1}
						className={`px-4 py-2 font-semibold text-white rounded-lg ${currentPage === 1 ? "bg-gray-400 cursor-not-allowed" : "bg-blue-500 hover:bg-blue-700"}`}
					>
						Previous
					</button>
					<p className="text-gray-700 dark:text-gray-300">{`Page ${currentPage} of ${totalPages}`}</p>
					<button
						type="button"
						onClick={handleNextPage}
						disabled={currentPage === totalPages}
						className={`px-4 py-2 font-semibold text-white rounded-lg ${currentPage === totalPages ? "bg-gray-400 cursor-not-allowed" : "bg-blue-500 hover:bg-blue-700"}`}
					>
						Next
					</button>
				</div>
			)}
		</div>
	);
};

export default TaskList;
