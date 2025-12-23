import type { ReactElement } from "react";
import TaskList from "./TaskList";

const Home = (): ReactElement => {
	return (
		<div className="App">
			<TaskList />
		</div>
	);
};

export default Home;
