import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useStore } from '../store/store';
import { buzz } from '../lib/util';

/** Port of OnboardingScreen — names are optional; everything stays on device. */
export function OnboardingScreen() {
  const nav = useNavigate();
  const { update } = useStore();
  const [name, setName] = useState('');

  const start = () => {
    buzz();
    update((s) => ({
      ...s,
      name: name.trim() || 'Irssa',
      welcomed: true,
    }));
    nav('/');
  };

  return (
    <div className="onboarding">
      <img className="onb-mascot" src="./img/mascot.png" alt="Irssa mascot" />
      <h1 className="urdu-inline">
        <span className="brand-en">Irssa</span> · <span className="brand-ur">ارسا</span>
      </h1>
      <p className="onb-tag">
        Learn Pakistan Sign Language — a game made for curious hands 🌈
      </p>

      <div className="onb-card">
        <div className="onb-step">1</div>
        <div className="onb-text">
          <b>What should we call you?</b>
          <input
            className="text-input"
            placeholder="Your name (optional)"
            value={name}
            maxLength={24}
            onChange={(e) => setName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && start()}
          />
        </div>
      </div>

      <div className="onb-card">
        <div className="onb-step">2</div>
        <div className="onb-text">
          <b>A camera that cheers for you</b>
          <span>
            When you practise with the camera, everything is checked on this device.
            Pictures are never saved and never sent — your practice belongs to you alone.
          </span>
        </div>
      </div>

      <button className="btn primary big" onClick={start}>
        Let's learn together! 🌟
      </button>
      <p className="onb-note">No accounts. No ads. No analytics. Works offline.</p>
    </div>
  );
}
