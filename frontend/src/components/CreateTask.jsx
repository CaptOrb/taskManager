import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
const CreateTask = () => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [dueDate, setDueDate] = useState('');
    const [priority, setPriority] = useState('LOW'); // Default priority
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const navigate = useNavigate();

    const handleTask = async (e) => {
        e.preventDefault();

        try {
            setError('');
            await axios.post('/api/create/task', 
                { 
                  title, 
                  description, 
                  dueDate, 
                  priority 
                },
                {
                  headers: {
                    Authorization: `Bearer ${localStorage.getItem('token')}` // Add JWT token to the request
                  }
                }
            );

            setSuccessMessage('Task created successfully.');
            navigate('/?success=true'); // Redirect after success
        } catch (error) {
            setSuccessMessage('');
            if (error.response) {
                setError(`Task failed: ${error.response.data || error.response.statusText}`);
                console.error('Task failed with response:', error.response);
            } else if (error.request) {
                setError('Task failed: No response received from server');
                console.error('Task failed without response:', error.request);
            } else {
                setError(`Task failed: ${error.message}`);
                console.error('Task error message:', error.message);
            }
        }
    };

    return (
        <div className="max-w-sm mx-auto">
            <form onSubmit={handleTask} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4 dark:bg-gray-800">
                {successMessage && (
                    <p className="text-green-500 mb-4">{successMessage}</p>
                )}
                {error && (
                    <p className="text-red-500 mb-4">{error}</p>
                )}

                <div className="mb-4">
                    <label htmlFor="title" className="block text-sm font-medium text-gray-700 dark:text-white">Title</label>
                    <input
                        type="text"
                        id="title"
                        placeholder="Title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                        className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    />
                </div>
          
                <div className="mb-4">
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 dark:text-white">Description</label>
            <textarea
                id="description"
                rows="4"
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
                    <label htmlFor="dueDate" className="block text-sm font-medium text-gray-700 dark:text-white">Due Date</label>
                    <input
                        type="datetime-local"
                        id="dueDate"
                        value={dueDate}
                        onChange={(e) => setDueDate(e.target.value)}
                        required
                        className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="priority" className="block text-sm font-medium text-gray-700 dark:text-white">Priority</label>
                    <select
                        id="priority"
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
