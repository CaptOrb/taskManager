import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../components/AuthContext';

const TaskList = () => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { loggedInUser } = useAuth();

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
      setTasks(response.data);
    } catch (error) {
      setError('Error fetching tasks: ' + (error.response?.data || error.message));
      console.error('Error fetching tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!loggedInUser) {
    return  <div className="flex items-center justify-center"> 
    <div className="bg-red-100 text-red-700 p-4 rounded-lg shadow-md max-w-md mx-auto"><p className="text-lg font-semibold text-center">Please log in to view tasks.</p>  </div>
      </div>;
  }

  if (loading) {
    return <div>Loading tasks...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>{error}</div>;
  }

  return (
    <div className="max-w-lg mx-auto mt-8 p-6 bg-white shadow-lg rounded-lg dark:bg-gray-800">
    <h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">Task List</h2>
    <ul className="space-y-4">
      {tasks.map((task) => (
        <li
          key={task.id}
          className="p-4 bg-gray-50 rounded-lg shadow-sm border border-gray-200 dark:bg-gray-700 dark:border-gray-600"
        >
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
          <span className="font-semibold">Title: </span>{task.title}
          </h3>
          <p className="text-gray-700 dark:text-gray-300">
            <span className="font-semibold">Description: </span> {task.description}
          </p>
          <p className="text-sm">
            <span className="font-semibold text-gray-500 dark:text-gray-400">Status: </span> {task.status}
          </p>
        </li>
      ))}
    </ul>
  </div>
  
  );
};

export default TaskList;