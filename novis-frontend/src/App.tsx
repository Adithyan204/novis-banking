import { BrowserRouter } from 'react-router-dom';
import { AppRouter } from './routes/AppRouter';

// AuthProvider is already in main.tsx — no need to double-wrap here
function App() {
  return (
    <BrowserRouter>
      <AppRouter />
    </BrowserRouter>
  );
}

export default App;