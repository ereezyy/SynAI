import React from 'react';
import './Features.css';

// Placeholder icons (ideally, use SVG icons or a library like Lucide React)
const IconPlaceholder = ({ name }: { name: string }) => <div className="icon-placeholder">{name.substring(0,1)}</div>;

const featuresData = [
  {
    icon: <IconPlaceholder name="Roulette" />,
    title: 'Discover Tailored Strategies',
    description: 'Spin our interactive wheel to find evidence-based coping techniques—from mindfulness to grounding exercises—personalized to your emotional state. Save your favorites for quick access.',
  },
  {
    icon: <IconPlaceholder name="AI Chat" />,
    title: 'Chat with a Compassionate AI',
    description: 'Our AI assistant offers a judgment-free space for emotional support, suggests personalized coping strategies, and helps you explore your feelings.',
  },
  {
    icon: <IconPlaceholder name="Location" />,
    title: 'Find Support Near You',
    description: 'Easily locate mental health services, support groups, and crisis resources in your area. Key information is available offline when you need it most.',
  },
  {
    icon: <IconPlaceholder name="Accessibility" />,
    title: 'Inclusive by Design',
    description: 'Full app functionality in English and Spanish, with screen reader compatibility, customizable text, and color contrast options to ensure everyone can benefit.',
  },
  {
    icon: <IconPlaceholder name="Offline" />,
    title: 'Always Available, Always Private',
    description: 'Access core coping skills and saved resources even without an internet connection. Your personal data stays private with local storage options.',
  },
];

const Features = () => {
  return (
    <section id="features" className="features-section">
      <div className="container">
        <h2>Your Pocket Companion for Mental Well-being</h2>
        <div className="features-grid">
          {featuresData.map((feature, index) => (
            <div key={index} className="feature-item">
              <div className="feature-icon">{feature.icon}</div>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default Features;
