import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../hooks/AuthContext';
import { Link } from 'react-router-dom';

const TaskList = () => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { loggedInUser } = useAuth();
  const [activeTab, setActiveTab] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const tasksPerPage = 5;
  const [statusFilter, setStatusFilter] = useState('ALL'); 
  const [urgencyFilter, setUrgencyFilter] = useState('ANY');
  const [sortOrder, setSortOrder] = useState('createdDate');
  
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
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      // Store fetched tasks without sorting
      setTasks(response.data);
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

  // Filter tasks based on active tab and filters
  let filteredTasks = tasks;

  if (statusFilter !== 'ALL') {
    filteredTasks = filteredTasks.filter(task => task.status === statusFilter);
  }

  if (urgencyFilter !== 'ANY') {
    filteredTasks = filteredTasks.filter(task => task.priority === urgencyFilter);
  }

  if (sortOrder === 'dueDateAsc') {
    filteredTasks.sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate));
  }   else if (sortOrder === 'createdDate') {
    filteredTasks.sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate));
  } 
  else if (sortOrder === 'dueDateDesc') {
    filteredTasks.sort((a, b) => new Date(b.dueDate) - new Date(a.dueDate));
  }

  // Pagination logic
  const indexOfLastTask = currentPage * tasksPerPage;
  const indexOfFirstTask = indexOfLastTask - tasksPerPage;
  const currentTasks = filteredTasks.slice(indexOfFirstTask, indexOfLastTask);
  const totalPages = Math.ceil(filteredTasks.length / tasksPerPage);

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
    <div className="max-w-xl mx-auto p-4 bg-white shadow-lg rounded-lg dark:bg-gray-800">
      <h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">Task List</h2>

      <div className="flex mt-4 mb-4">
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="border rounded p-2 mr-2"
        >
          <option value="ALL">All Statuses</option>
          <option value="PENDING">Pending</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="COMPLETED">Completed</option>
        </select>

        <select
          value={urgencyFilter}
          onChange={(e) => setUrgencyFilter(e.target.value)}
          className="border rounded p-2 mr-2"
        >
          <option value="ANY">Any Priority</option>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>

        <select
          value={sortOrder}
          onChange={(e) => setSortOrder(e.target.value)}
          className="border rounded p-2"
        >
          <option value="createdDate">Recently Created</option>
          <option value="dueDateAsc">Due Date: Ascending</option>
          <option value="dueDateDesc">Due Date: Descending</option>
        </select>

        <Link
          to="/createTask"
          className="ml-4 text-white bg-blue-500 hover:bg-blue-700 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-4 lg:px-5 py-2 lg:py-2.5 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none"
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
        <Link to={`/tasks/${task.id}`} className="hover:underline">
          <span className="font-semibold">Title: </span>{task.title}
        </Link>
      </h3>

            <p className="text-sm">
              <span className="font-semibold text-gray-500 dark:text-gray-400">Status: </span> {task.status}
            </p>
            <p className="text-sm">
              <span className="font-semibold text-gray-500 dark:text-gray-400">Created Date: </span> {new Date(task.createdDate).toLocaleString()}
            </p>
            <p className="text-sm">
              <span className="font-semibold text-gray-500 dark:text-gray-400">Due Date: </span> {new Date(task.dueDate).toLocaleString()}
            </p>
            <p className="text-sm">
              <span className="font-semibold text-gray-500 dark:text-gray-400">Priority: </span> {task.priority}
            </p>
          </li>
        ))}
      </ul>

      {/* Pagination Controls */}
      {totalPages > 1 && (
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
      )}
    </div>
  );
};

export default TaskList;
