import { Routes, Route } from "react-router-dom";
import VolunteerDashboard from "./VolunteerDashboard";

function VolunteerRoutes() {
  return (
    <Routes>
      <Route path="/" element={<VolunteerDashboard />} />
    </Routes>
  );
}

export default VolunteerRoutes;