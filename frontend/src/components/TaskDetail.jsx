import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/AuthContext';
import ReactMarkdown from 'react-markdown';

const TaskDetail = () => {
  const { id } = useParams();
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const { loggedInUser } = useAuth();
  const navigate = useNavigate();

  const [taskTitle, setTaskTitle] = useState('');
  const [taskDescription, setTaskDescription] = useState('');
  const [taskStatus, setTaskStatus] = useState('');
  const [taskPriority, setTaskPriority] = useState('');
  const [taskDueDate, setTaskDueDate] = useState('');

  useEffect(() => {
    const fetchTask = async () => {
      if (loggedInUser) {
        try {
          setLoading(true);
          const response = await axios.get(`/api/tasks/${id}`, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem('token')}`,
            },
          });
          const fetchedTask = response.data;
          setTask(fetchedTask);
          setTaskTitle(fetchedTask.title);
          setTaskDescription(fetchedTask.description);
          setTaskStatus(fetchedTask.status);
          setTaskPriority(fetchedTask.priority);
          setTaskDueDate(new Date(fetchedTask.dueDate).toISOString().slice(0, 16));
        } catch (error) {
          if (error.response?.status === 404  || error.response?.status === 403) {
            navigate('/');
          } else {
          setError('Error fetching task: ' + (error.response?.data || error.message));
          }
        } finally {
          setLoading(false);
        }
      } else {
        setLoading(false);
      }
    };

    fetchTask();
  }, [loggedInUser, id]);

  const handleUpdate = async () => {
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
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (response.status === 200) {
        setTask((prevTask) => ({
          ...prevTask,
          ...response.data,
        }));
        setError('');
        setIsEditing(false);
      }
    } catch (error) {
      setError('Error updating task: ' + (error.response?.data || error.message));
    }
  };

  const handleDelete = async () => {
    const confirmDelete = window.confirm("Are you sure you want to delete this task?");
    if (confirmDelete) {
      try {
        await axios.delete(`/api/tasks/delete/${id}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
          },
        });
        navigate('/');
      } catch (error) {
        setError('Error deleting task: ' + (error.response?.data || error.message));
      }
    }
  };

  if (loading) {
    return <div>Loading task...</div>;
  }

  if (error && !isEditing) {
    return <div style={{ color: 'red' }}>{error}</div>;
  }

  if (!task) {
    return <div>No task found.</div>;
  }

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
          <h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">Edit Task</h2>

          {error && <p style={{ color: 'red' }}>{error}</p>}

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-white">Title</label>
            <input
              type="text"
              value={taskTitle}
              onChange={(e) => setTaskTitle(e.target.value)}
              className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-white">Description</label>
            <textarea
              value={taskDescription}
              onChange={(e) => setTaskDescription(e.target.value)}
              className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:text-white " wrap="hard"
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-white">Description Preview</label>
            <div className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:text-white break-words">
              <ReactMarkdown>{taskDescription}</ReactMarkdown>
            </div>
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-white">Status</label>
            <select
              value={taskStatus}
              onChange={(e) => setTaskStatus(e.target.value)}
              className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            >
              <option value="PENDING">Pending</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-white">Priority</label>
            <select
              value={taskPriority}
              onChange={(e) => setTaskPriority(e.target.value)}
              className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-white">Due Date</label>
            <input
              type="datetime-local"
              value={taskDueDate}
              onChange={(e) => setTaskDueDate(e.target.value)}
              className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            />
          </div>

          <button
            onClick={handleUpdate}
            className="text-white bg-green-700 hover:bg-green-800 font-medium rounded-lg text-sm px-5 py-2.5 mr-2 mb-2"
          >
            Save Changes
          </button>

          <button
            onClick={() => setIsEditing(false)}
            className="text-white bg-red-700 hover:bg-red-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2"
          >
            Cancel
          </button>
        </div>
      ) : (
        <div className="max-w-2xl mx-auto mt-8 p-8 bg-white shadow-lg rounded-lg dark:bg-gray-800">
          <h2 className="text-3xl font-bold mb-6 text-gray-900 dark:text-white">{task.title}</h2>

          <div className="mb-6">
            <div className="mb-4">
              <p className="text-gray-700 dark:text-gray-300 font-semibold mb-1">Description:</p>

              <div className="bg-gray-50 border border-gray-300 p-4 rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white break-words">
                <ReactMarkdown>{task.description}</ReactMarkdown>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 mb-6">
              <div>
                <p className="text-sm font-semibold text-gray-500 dark:text-gray-400">Status:</p>
                <p className="text-gray-900 dark:text-gray-300">{task.status}</p>
              </div>

              <div>
                <p className="text-sm font-semibold text-gray-500 dark:text-gray-400">Priority:</p>
                <p className="text-gray-900 dark:text-gray-300">{task.priority}</p>
              </div>

              <div>
                <p className="text-sm font-semibold text-gray-500 dark:text-gray-400">Created Date:</p>
                <p className="text-gray-900 dark:text-gray-300">{new Date(task.createdDate).toLocaleString()}</p>
              </div>

              <div>
                <p className="text-sm font-semibold text-gray-500 dark:text-gray-400">Due Date:</p>
                <p className="text-gray-900 dark:text-gray-300">{new Date(task.dueDate).toLocaleString()}</p>
              </div>
            </div>

            <div className="flex space-x-4">
              <button
                onClick={() => setIsEditing(true)}
                className="flex-1 bg-yellow-500 text-white font-medium rounded-lg text-sm px-5 py-2.5 hover:bg-yellow-600"
              >
                Edit Task
              </button>

              <button
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
