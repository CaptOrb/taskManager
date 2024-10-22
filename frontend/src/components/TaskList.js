import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../hooks/AuthContext';
import { Link } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';

const TaskList = () => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { loggedInUser } = useAuth();
  const [activeTab, setActiveTab] = useState('all'); // State to manage active tab
  const [currentPage, setCurrentPage] = useState(1); // Current page
  const tasksPerPage = 5; // Number of tasks per page

  useEffect(() => {
    if (loggedInUser) {
      fetchTasks();
    } else {
      setLoading(false);
    }
  }, [loggedInUser]);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/tasks', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}` // Add JWT token to the request
        }
      });

      const sortedTasks = response.data.sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate));
      setTasks(sortedTasks);
    } catch (error) {
      setError('Error fetching tasks: ' + (error.response?.data || error.message));
      console.error('Error fetching tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!loggedInUser) {
    return (
      <div className="flex items-center justify-center">
        <div className="bg-red-100 text-red-700 p-4 rounded-lg shadow-md max-w-md mx-auto">
          <p className="text-lg font-semibold text-center">Please log in to view tasks.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return <div>Loading tasks...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>{error}</div>;
  }

  // Filter tasks based on active tab
  const filteredTasks = activeTab === 'completed' 
    ? tasks.filter(task => task.status === 'COMPLETED') 
    : tasks; // Display all tasks if not in completed tab

  // Pagination logic
  const indexOfLastTask = currentPage * tasksPerPage;
  const indexOfFirstTask = indexOfLastTask - tasksPerPage;
  const currentTasks = filteredTasks.slice(indexOfFirstTask, indexOfLastTask);
  const totalPages = Math.ceil(filteredTasks.length / tasksPerPage);
  const completedTaskCount = tasks.filter(task => task.status === 'COMPLETED').length;
  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  const handlePreviousPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  return (
    <div className="max-w-lg mx-auto mt-8 p-6 bg-white shadow-lg rounded-lg dark:bg-gray-800">
      <h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">Task List</h2>

      <div className="flex mb-4">
        <button
          onClick={() => setActiveTab('all')}
          className={`px-4 py-2 font-semibold ${activeTab === 'all' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}
        >
          All Tasks
        </button>
        {completedTaskCount > 0 && (
          <button
            onClick={() => setActiveTab('completed')}
            className={`ml-2 px-4 py-2 font-semibold ${activeTab === 'completed' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}
          >
            Completed Tasks
          </button>
        )}
        <Link
          to="/createTask"
          className="ml-4 text-gray-800 dark:text-white hover:bg-gray-50 focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-sm px-4 lg:px-5 py-2 lg:py-2.5 dark:hover:bg-gray-700 focus:outline-none dark:focus:ring-gray-800"
        >
          Create Task
        </Link>
      </div>

      <ul className="space-y-4">
        {currentTasks.map((task) => (
          <li
            key={task.id}
            className="p-4 bg-gray-50 rounded-lg shadow-sm border border-gray-200 dark:bg-gray-700 dark:border-gray-600"
          >
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
              <span className="font-semibold">Title: </span>{task.title}
            </h3>
            <p className="text-gray-700 dark:text-gray-300">
              <span className="font-semibold">Description: </span>
              <ReactMarkdown>{task.description}</ReactMarkdown>
            </p>
            <p className="text-sm">
              <span className="font-semibold text-gray-500 dark:text-gray-400">Status: </span> {task.status}
            </p>
            <p className="text-sm">
              <span className="font-semibold text-gray-500 dark:text-gray-400">Created Date: </span> {new Date(task.createdDate).toLocaleString()}
            </p>
            <p className="text-sm">
              <span className="font-semibold text-gray-500 dark:text-gray-400"> Priority: </span> {task.priority}
            </p>
          </li>
        ))}
      </ul>

      {/* Pagination Controls */}
      <div className="flex justify-between mt-4">
        <button
          onClick={handlePreviousPage}
          disabled={currentPage === 1}
          className={`px-4 py-2 font-semibold text-white rounded-lg ${currentPage === 1 ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-500 hover:bg-blue-700'}`}
        >
          Previous
        </button>
        <p className="text-gray-700 dark:text-gray-300">{`Page ${currentPage} of ${totalPages}`}</p>
        <button
          onClick={handleNextPage}
          disabled={currentPage === totalPages}
          className={`px-4 py-2 font-semibold text-white rounded-lg ${currentPage === totalPages ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-500 hover:bg-blue-700'}`}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default TaskList;
