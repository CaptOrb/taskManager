import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/AuthContext';

const TaskDetail = () => {
  const { id } = useParams();
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { loggedInUser } = useAuth();

  useEffect(() => {
    const fetchTask = async () => {
      if (loggedInUser) {
        try {
          setLoading(true);
          const response = await axios.get(`/api/tasks/${id}`, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem('token')}`
            }
          });
          setTask(response.data);
        } catch (error) {
          setError('Error fetching task: ' + (error.response?.data || error.message));
          console.error('Error fetching task:', error);
        } finally {
          setLoading(false);
        }
      } else {
        setLoading(false);
      }
    };

    fetchTask();
  }, [loggedInUser, id]);

  if (loading) {
    return <div>Loading task...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>{error}</div>;
  }

  if (!task) {
    return <div>No task found.</div>;
  }

  return (
    <div className="max-w-xl mx-auto mt-8 p-6 bg-white shadow-lg rounded-lg dark:bg-gray-800">
      <Link to="/">
        <button
          type="button"
          className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800"
        >
          Back
        </button>
      </Link>

      <h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">{task.title}</h2>
      <p className="text-gray-700 dark:text-gray-300">
        <span className="font-semibold">Description: </span>
        {task.description}
      </p>
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
    </div>
  );
};

export default TaskDetail;
