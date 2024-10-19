import React from 'react';
import TaskList from './TaskList';


const Home = () => {
  return (
    <div className="App">
      <header className="App-header">
        <h1>Spring Boot + React Task Management</h1>
        <TaskList />
      </header>
    </div>
  );
};

export default Home;