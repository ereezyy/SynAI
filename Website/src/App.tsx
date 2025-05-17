import React from 'react';
import './App.css';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Home from './components/Home';
import Features from './components/Features';
import About from './components/About';
import Press from './components/Press';
import Download from './components/Download';
import Contact from './components/Contact';

function App() {
  return (
    <div className="App">
      <Navbar />
      <main>
        <Home />
        <Features />
        <About />
        <Press />
        <Download />
        <Contact />
      </main>
      <Footer />
    </div>
  );
}

export default App;
