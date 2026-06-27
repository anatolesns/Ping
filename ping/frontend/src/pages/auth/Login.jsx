import React from 'react';
import { useNavigate } from 'react-router-dom';

function Login() {
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    // navigate('/benevole'); // Pour tester
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-green-100 p-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-2xl p-8 border border-gray-200">
          <div className="flex flex-col items-center mb-8">
            <div className="bg-gradient-to-br from-green-600 to-green-600 p-4 rounded-full mb-4 shadow-lg">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-leaf w-12 h-12 text-white">
                <path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z"></path>
                <path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"></path>
              </svg>
            </div>
            <h1 className="text-center mb-2 text-gray-900 text-2xl font-semibold">Salon Décarbonisation</h1>
            <p className="text-gray-500 text-center">Eurexpo Lyon</p>
          </div>
          
          <form className="space-y-6" onSubmit={handleLogin}>
            <div>
              <label htmlFor="email" className="block mb-2 text-gray-700 font-medium">Adresse mail</label>
              <input id="email" type="text" className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 text-gray-900" placeholder="test@example.com" required />
            </div>
            <div>
              <label htmlFor="password" className="block mb-2 text-gray-700 font-medium">Mot de passe</label>
              <input id="password" type="password" className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 text-gray-900" placeholder="••••••••" required />
            </div>
            <button type="submit" className="w-full bg-gradient-to-r from-green-600 to-green-600 hover:from-green-700 hover:to-green-700 text-white py-3 rounded-lg transition-all shadow-md hover:shadow-lg flex items-center justify-center gap-2 font-medium">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-log-in w-5 h-5">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                <polyline points="10 17 15 12 10 7"></polyline>
                <line x1="15" x2="3" y1="12" y2="12"></line>
              </svg>
              Se connecter
            </button>
          </form>

          {/* Liens de dev */}
          <div className="mt-8 pt-4 border-t border-gray-100 flex justify-center gap-4">
            <button type="button" onClick={() => navigate('/admin')} className="text-xs text-gray-500 hover:text-gray-900">Admin</button>
            <button type="button" onClick={() => navigate('/benevole')} className="text-xs text-gray-500 hover:text-gray-900">Bénévole</button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
