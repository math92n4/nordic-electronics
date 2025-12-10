import { useState, useEffect } from "react";
import { useAuth } from "../hooks/useAuth";

export function AuthModal({ isOpen, onClose, onSuccess }) {
  const [activeTab, setActiveTab] = useState("login");
  const {
    login,
    loginAsync,
    isLoggingIn,
    loginError,
    register,
    registerAsync,
    isRegistering,
    registerError,
  } = useAuth();

  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    dateOfBirth: "",
    password: "",
  });

  // Handle body overflow when modal is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "auto";
    }
    return () => {
      document.body.style.overflow = "auto";
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const handleLogin = async e => {
    e.preventDefault();
    try {
      const result = await loginAsync({
        email: loginForm.email,
        password: loginForm.password,
      });
      if (result.success) {
        setLoginForm({ email: "", password: "" });
        onSuccess?.("Login successful!", "success");
        onClose();
      }
    } catch (error) {
      onSuccess?.(error.message || "Login failed", "error");
    }
  };

  const handleRegister = async e => {
    e.preventDefault();

    if (
      !registerForm.firstName ||
      !registerForm.lastName ||
      !registerForm.email ||
      !registerForm.phoneNumber ||
      !registerForm.dateOfBirth ||
      !registerForm.password
    ) {
      onSuccess?.("Please fill in all fields", "error");
      return;
    }

    if (registerForm.password.length < 6) {
      onSuccess?.("Password must be at least 6 characters long", "error");
      return;
    }

    try {
      await registerAsync(registerForm);
      onSuccess?.("Registration successful! Please login.", "success");
      setRegisterForm({
        firstName: "",
        lastName: "",
        email: "",
        phoneNumber: "",
        dateOfBirth: "",
        password: "",
      });
      setActiveTab("login");
    } catch (error) {
      onSuccess?.(error.message || "Registration failed", "error");
    }
  };

  return (
    <div
      id="auth-modal"
      className="modal"
      style={{ display: isOpen ? "flex" : "none" }}
      onClick={e => e.target.id === "auth-modal" && onClose()}
    >
      <div className="modal-content">
        <span className="close" onClick={onClose}>
          &times;
        </span>
        <div className="auth-container">
          <div className="auth-tabs">
            <button
              className={`tab-button ${activeTab === "login" ? "active" : ""}`}
              onClick={() => setActiveTab("login")}
            >
              Login
            </button>
            <button
              className={`tab-button ${
                activeTab === "register" ? "active" : ""
              }`}
              onClick={() => setActiveTab("register")}
            >
              Register
            </button>
          </div>

          {activeTab === "login" && (
            <div id="login-tab" className="tab-content active">
              <h3>Login to Your Account</h3>
              <form id="login-form" onSubmit={handleLogin}>
                <div className="form-group">
                  <input
                    type="email"
                    id="login-email"
                    placeholder="Email"
                    required
                    value={loginForm.email}
                    onChange={e =>
                      setLoginForm({ ...loginForm, email: e.target.value })
                    }
                  />
                </div>
                <div className="form-group">
                  <input
                    type="password"
                    id="login-password"
                    placeholder="Password"
                    required
                    value={loginForm.password}
                    onChange={e =>
                      setLoginForm({ ...loginForm, password: e.target.value })
                    }
                  />
                </div>
                {loginError && (
                  <p className="error-text">{loginError.message}</p>
                )}
                <button
                  type="submit"
                  className="auth-button"
                  disabled={isLoggingIn}
                >
                  {isLoggingIn ? "Logging in..." : "Login"}
                </button>
              </form>
            </div>
          )}

          {activeTab === "register" && (
            <div id="register-tab" className="tab-content active">
              <h3>Create Account</h3>
              <form id="register-form" onSubmit={handleRegister}>
                <div className="form-group">
                  <input
                    type="text"
                    id="register-firstName"
                    placeholder="First Name"
                    required
                    value={registerForm.firstName}
                    onChange={e =>
                      setRegisterForm({
                        ...registerForm,
                        firstName: e.target.value,
                      })
                    }
                  />
                </div>
                <div className="form-group">
                  <input
                    type="text"
                    id="register-lastName"
                    placeholder="Last Name"
                    required
                    value={registerForm.lastName}
                    onChange={e =>
                      setRegisterForm({
                        ...registerForm,
                        lastName: e.target.value,
                      })
                    }
                  />
                </div>
                <div className="form-group">
                  <input
                    type="email"
                    id="register-email"
                    placeholder="Email"
                    required
                    value={registerForm.email}
                    onChange={e =>
                      setRegisterForm({
                        ...registerForm,
                        email: e.target.value,
                      })
                    }
                  />
                </div>
                <div className="form-group">
                  <input
                    type="tel"
                    id="register-phone"
                    placeholder="Phone Number"
                    required
                    pattern="\d{8}"
                    title="Please enter a valid 8-digit Danish mobile number"
                    value={registerForm.phoneNumber}
                    onChange={e =>
                      setRegisterForm({
                        ...registerForm,
                        phoneNumber: e.target.value,
                      })
                    }
                  />
                  <small style={{ fontSize: "0.85em", color: "#666" }}>
                    Danish phone number
                  </small>
                </div>
                <div className="form-group">
                  <label
                    htmlFor="register-dateOfBirth"
                    style={{
                      fontSize: "0.9em",
                      color: "#666",
                      marginBottom: "5px",
                      display: "block",
                    }}
                  >
                    Date of Birth *
                  </label>
                  <input
                    type="date"
                    id="register-dateOfBirth"
                    required
                    value={registerForm.dateOfBirth}
                    onChange={e =>
                      setRegisterForm({
                        ...registerForm,
                        dateOfBirth: e.target.value,
                      })
                    }
                    max={new Date().toISOString().split("T")[0]}
                  />
                </div>
                <div className="form-group">
                  <input
                    type="password"
                    id="register-password"
                    placeholder="Password"
                    required
                    value={registerForm.password}
                    onChange={e =>
                      setRegisterForm({
                        ...registerForm,
                        password: e.target.value,
                      })
                    }
                  />
                </div>
                {registerError && (
                  <p className="error-text">{registerError.message}</p>
                )}
                <button
                  type="submit"
                  className="auth-button"
                  disabled={isRegistering}
                >
                  {isRegistering ? "Registering..." : "Register"}
                </button>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
