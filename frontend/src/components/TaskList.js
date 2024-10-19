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
    return <div>Please log in to view tasks.</div>; 
  }

  if (loading) {
    return <div>Loading tasks...</div>;
  }

  if (error) {
    return <div style={{ color: 'red' }}>{error}</div>;
  }

  return (
    <div>
      <h2>Task List</h2>
      <ul>
        {tasks.map(task => (
          <li key={task.id}>Title - {task.title} Description - {task.description} - Status - {task.status}</li>
        ))}
      </ul>
    </div>
  );
};

export default TaskList;