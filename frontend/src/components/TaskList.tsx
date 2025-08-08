import { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../hooks/AuthContext';
import { Link } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { Task } from '../types';
import LoadingSpinner from './LoadingSpinner';

const TaskList = () => {
    const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const { loggedInUser } = useAuth();
  const [currentPage, setCurrentPage] = useState(1);
  const tasksPerPage = 5;
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [urgencyFilter, setUrgencyFilter] = useState('ANY');
  const [sortOrder, setSortOrder] = useState('createdDate');
  const navigate = useNavigate();

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
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      // Check if the response data is an array
      if (Array.isArray(response.data)) {
        setTasks(response.data);
      } else {
        console.error('Expected an array but received:', response.data);
        setError('Unexpected data format.');
      }
    } catch (error) {
      setError('Error fetching tasks: ');
      // setError('Error fetching tasks: ' + (error.response?.data || error.message));
      console.error('Error fetching tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!loggedInUser) {
    return (
      <div className="flex items-center justify-center p-4">
        <div className="bg-red-100 text-red-700 p-4 rounded-lg shadow-md max-w-md mx-auto">
          <p className="text-lg font-semibold text-center">Please log in to view tasks.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return <LoadingSpinner text="Loading tasks..." />;
  }

  if (error) {
    return (
      <div className="max-w-xl mx-auto p-4">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">Error loading tasks</h3>
              <div className="mt-2 text-sm text-red-700">{error}</div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const getFilteredAndSortedTasks = () => {
    let filteredTasks = tasks;

    // Apply filters
    if (statusFilter !== 'ALL') {
      filteredTasks = filteredTasks.filter(task => task.status === statusFilter);
    }

    if (urgencyFilter !== 'ANY') {
      filteredTasks = filteredTasks.filter(task => task.priority === urgencyFilter);
    }

    // Sorting logic
    if (Array.isArray(filteredTasks)) {
      if (sortOrder === 'dueDateAsc') {
        return filteredTasks.sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime());
      } else if (sortOrder === 'createdDate') {
        return filteredTasks.sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime());
      } else if (sortOrder === 'dueDateDesc') {
        return filteredTasks.sort((a, b) => new Date(b.dueDate).getTime() - new Date(a.dueDate).getTime());
      }
    }

    return filteredTasks; // Return the filtered array
  };

  // Pagination logic
  const filteredTasks = getFilteredAndSortedTasks();
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

  // Status badge component
  const StatusBadge = ({ status }: { status: string }) => {
    const statusConfig = {
      'PENDING': { color: 'bg-gray-100 text-gray-800 border-gray-300', icon: '⏳' },
      'IN_PROGRESS': { color: 'bg-blue-100 text-blue-800 border-blue-300', icon: '🔄' },
      'COMPLETED': { color: 'bg-green-100 text-green-800 border-green-300', icon: '✅' },
    };
    
    const config = statusConfig[status as keyof typeof statusConfig] || statusConfig['PENDING'];
    
    return (
      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${config.color}`}>
        <span className="mr-1">{config.icon}</span>
        <span className="hidden sm:inline">{status.replace('_', ' ')}</span>
        <span className="sm:hidden">{status.replace('_', ' ').split(' ')[0]}</span>
      </span>
    );
  };

  // Priority badge component
  const PriorityBadge = ({ priority }: { priority: string }) => {
    const priorityConfig = {
      'LOW': { color: 'bg-green-100 text-green-800 border-green-300', icon: '🟢' },
      'MEDIUM': { color: 'bg-yellow-100 text-yellow-800 border-yellow-300', icon: '🟡' },
      'HIGH': { color: 'bg-red-100 text-red-800 border-red-300', icon: '🔴' },
    };
    
    const config = priorityConfig[priority as keyof typeof priorityConfig] || priorityConfig['LOW'];
    
    return (
      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${config.color}`}>
        <span className="mr-1">{config.icon}</span>
        <span className="hidden sm:inline">{priority}</span>
        <span className="sm:hidden">{priority.charAt(0)}</span>
      </span>
    );
  };

  return (
    <div className="max-w-4xl mx-auto p-2 sm:p-4 lg:p-6 bg-white shadow-lg rounded-lg dark:bg-gray-800">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4 sm:mb-6">
        <h2 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-0">Task List</h2>
        <Link
          to="/createTask"
          className="inline-flex items-center px-3 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg text-sm transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          <span className="hidden sm:inline">Create Task</span>
          <span className="sm:hidden">Add</span>
        </Link>
      </div>

      {/* Filters Section */}
      <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-3 sm:p-4 mb-4 sm:mb-6">
        <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2 sm:mb-3">Filters & Sorting</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
          {/* Status Filter */}
          <div>
            <label className="block text-xs sm:text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Status
            </label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="block w-full px-2 py-1.5 sm:px-3 sm:py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-600 dark:border-gray-500 dark:text-white text-sm"
            >
              <option value="ALL">All Statuses</option>
              <option value="PENDING">⏳ Pending</option>
              <option value="IN_PROGRESS">🔄 In Progress</option>
              <option value="COMPLETED">✅ Completed</option>
            </select>
          </div>

          {/* Priority Filter */}
          <div>
            <label className="block text-xs sm:text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Priority
            </label>
            <select
              value={urgencyFilter}
              onChange={(e) => setUrgencyFilter(e.target.value)}
              className="block w-full px-2 py-1.5 sm:px-3 sm:py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-600 dark:border-gray-500 dark:text-white text-sm"
            >
              <option value="ANY">Any Priority</option>
              <option value="LOW">🟢 Low</option>
              <option value="MEDIUM">🟡 Medium</option>
              <option value="HIGH">🔴 High</option>
            </select>
          </div>

          {/* Sort Order */}
          <div className="sm:col-span-2 lg:col-span-1">
            <label className="block text-xs sm:text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Sort By
            </label>
            <select
              value={sortOrder}
              onChange={(e) => setSortOrder(e.target.value)}
              className="block w-full px-2 py-1.5 sm:px-3 sm:py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 bg-white dark:bg-gray-600 dark:border-gray-500 dark:text-white text-sm"
            >
              <option value="createdDate">📅 Recently Created</option>
              <option value="dueDateAsc">⏰ Due Date: Earliest First</option>
              <option value="dueDateDesc">⏰ Due Date: Latest First</option>
            </select>
          </div>
        </div>
      </div>

      {/* Results Summary */}
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center mb-3 sm:mb-4 gap-2">
        <p className="text-xs sm:text-sm text-gray-600 dark:text-gray-400">
          Showing {currentTasks.length} of {filteredTasks.length} tasks
        </p>
        {filteredTasks.length > 0 && (
          <div className="flex flex-wrap gap-1 sm:gap-2">
            {statusFilter !== 'ALL' && (
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                Status: {statusFilter.replace('_', ' ')}
              </span>
            )}
            {urgencyFilter !== 'ANY' && (
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                Priority: {urgencyFilter}
              </span>
            )}
          </div>
        )}
      </div>

      {/* Task List */}
      {currentTasks.length === 0 ? (
        <div className="text-center py-6 sm:py-8">
          <svg className="mx-auto h-8 w-8 sm:h-12 sm:w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900 dark:text-white">No tasks found</h3>
          <p className="mt-1 text-xs sm:text-sm text-gray-500 dark:text-gray-400">
            {filteredTasks.length === 0 && tasks.length > 0 
              ? 'Try adjusting your filters.' 
              : 'Get started by creating a new task.'}
          </p>
        </div>
      ) : (
        <ul className="space-y-3 sm:space-y-4">
          {currentTasks.map((task) => (
            <li
              key={task.id}
              className="p-3 sm:p-4 lg:p-6 bg-white dark:bg-gray-700 rounded-lg shadow-sm border border-gray-200 dark:border-gray-600 cursor-pointer hover:shadow-md transition-shadow duration-200 hover:bg-gray-50 dark:hover:bg-gray-600"
              onClick={() => navigate(`/tasks/${task.id}`)}
            >
              <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-3">
                <div className="flex-1 min-w-0">
                  <h3 className="text-base sm:text-lg font-semibold text-gray-900 dark:text-white mb-2 truncate">
                    {task.title}
                  </h3>
                  
                  <div className="flex flex-wrap gap-1 sm:gap-2 mb-2 sm:mb-3">
                    <StatusBadge status={task.status} />
                    <PriorityBadge priority={task.priority} />
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-1 sm:gap-2 text-xs sm:text-sm text-gray-600 dark:text-gray-400">
                    <div className="flex items-center">
                      <svg className="w-3 h-3 sm:w-4 sm:h-4 mr-1 sm:mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      <span className="truncate">Created: {new Date(task.createdDate).toLocaleDateString()}</span>
                    </div>
                    <div className="flex items-center">
                      <svg className="w-3 h-3 sm:w-4 sm:h-4 mr-1 sm:mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <span className="truncate">Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                    </div>
                  </div>
                </div>

                <div className="flex justify-end sm:ml-4">
                  <Link 
                    to={`/tasks/${task.id}`} 
                    className="inline-flex items-center px-2 py-1 sm:px-3 sm:py-1.5 border border-transparent text-xs font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <span className="hidden sm:inline">View Details</span>
                    <span className="sm:hidden">View</span>
                    <svg className="ml-1 w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </Link>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex flex-col sm:flex-row justify-between items-center mt-4 sm:mt-6 gap-3">
          <button
            onClick={handlePreviousPage}
            disabled={currentPage === 1}
            className={`inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md transition-colors duration-200 ${
              currentPage === 1 
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                : 'bg-blue-600 text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500'
            }`}
          >
            <svg className="w-4 h-4 mr-1 sm:mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            <span className="hidden sm:inline">Previous</span>
            <span className="sm:hidden">Prev</span>
          </button>
          
          <div className="flex items-center space-x-1 sm:space-x-2">
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
              <button
                key={page}
                onClick={() => setCurrentPage(page)}
                className={`px-2 py-1 sm:px-3 sm:py-1 text-xs sm:text-sm font-medium rounded-md transition-colors duration-200 ${
                  currentPage === page
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-gray-600 dark:text-gray-300 dark:hover:bg-gray-500'
                }`}
              >
                {page}
              </button>
            ))}
          </div>

          <button
            onClick={handleNextPage}
            disabled={currentPage === totalPages}
            className={`inline-flex items-center px-3 py-2 border border-transparent text-sm font-medium rounded-md transition-colors duration-200 ${
              currentPage === totalPages 
                ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                : 'bg-blue-600 text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500'
            }`}
          >
            <span className="hidden sm:inline">Next</span>
            <span className="sm:hidden">Next</span>
            <svg className="w-4 h-4 ml-1 sm:ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      )}
    </div>
  );
};

export default TaskList;
