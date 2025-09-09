import axios from "axios";
import { type FormEvent, useId, useState } from "react";
import ReactMarkdown from "react-markdown";
import { useNavigate } from "react-router-dom";

const CreateTask = () => {
	const [title, setTitle] = useState("");
	const [description, setDescription] = useState("");
	const [dueDate, setDueDate] = useState("");
	const [priority, setPriority] = useState("LOW");
	const [error, setError] = useState("");
	const [successMessage, setSuccessMessage] = useState("");
	const navigate = useNavigate();

	const titleId = useId();
	const descriptionId = useId();
	const dueDateId = useId();
	const priorityId = useId();

	const handleTask = async (e: FormEvent<HTMLFormElement>) => {
		e.preventDefault();

		try {
			setError("");
			await axios.post(
				"/api/create/task",
				{ title, description, dueDate, priority },
				{
					headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
				},
			);
			setSuccessMessage("Task created successfully.");
			navigate("/?success=true");
		} catch (error) {
			setSuccessMessage("");
			if (error instanceof Error) setError(`Task failed: ${error.message}`);
			else setError("Task failed: Unknown error occurred");
		}
	};

	return (
		<div className="max-w-sm mx-auto">
			<form
				onSubmit={handleTask}
				className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4 dark:bg-gray-800"
			>
				{successMessage && (
					<p className="text-green-500 mb-4">{successMessage}</p>
				)}
				{error && <p className="text-red-500 mb-4">{error}</p>}

				<div className="mb-4">
					<label
						htmlFor={titleId}
						className="block text-sm font-medium text-gray-700 dark:text-white"
					>
						Title
					</label>
					<input
						type="text"
						id={titleId}
						placeholder="Title"
						value={title}
						onChange={(e) => setTitle(e.target.value)}
						required
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					/>
				</div>

				<div className="mb-4">
					<label
						htmlFor={descriptionId}
						className="block text-sm font-medium text-gray-700 dark:text-white"
					>
						Description
					</label>
					<textarea
						id={descriptionId}
						rows={4}
						placeholder="Enter description (supports Markdown)"
						value={description}
						onChange={(e) => setDescription(e.target.value)}
						required
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					></textarea>
					<h4 className="mt-4 text-lg font-semibold">Preview:</h4>
					<div className="p-4 bg-gray-100 border rounded-lg dark:bg-gray-800 dark:text-white break-words whitespace-pre-wrap">
						<ReactMarkdown>{description}</ReactMarkdown>
					</div>
				</div>

				<div className="mb-4">
					<label
						htmlFor={dueDateId}
						className="block text-sm font-medium text-gray-700 dark:text-white"
					>
						Due Date
					</label>
					<input
						type="datetime-local"
						id={dueDateId}
						value={dueDate}
						onChange={(e) => setDueDate(e.target.value)}
						required
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					/>
				</div>

				<div className="mb-4">
					<label
						htmlFor={priorityId}
						className="block text-sm font-medium text-gray-700 dark:text-white"
					>
						Priority
					</label>
					<select
						id={priorityId}
						value={priority}
						onChange={(e) => setPriority(e.target.value)}
						required
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					>
						<option value="LOW">Low</option>
						<option value="MEDIUM">Medium</option>
						<option value="HIGH">High</option>
					</select>
				</div>

				<button
					type="submit"
					className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
				>
					Create Task
				</button>
			</form>
		</div>
	);
};

export default CreateTask;
