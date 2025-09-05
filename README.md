# 📱 Subscription Remover

**Take control of your subscriptions** - A smart Android app that helps users track, manage, and cancel subscriptions across all platforms.

![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Kotlin-blue)
![Architecture](https://img.shields.io/badge/architecture-MVVM-orange)
![License](https://img.shields.io/badge/license-MIT-brightgreen)

## 🚀 **Features**

### ✅ **Current Features**
- **🔐 Secure Authentication** - Firebase Auth with email/password and Google Sign-In
- **📊 Beautiful Dashboard** - Overview of active subscriptions and spending
- **📂 Category Organization** - Entertainment, Social Media, Productivity, Fitness, News, Miscellaneous
- **💳 Smart Tracking** - Monitor subscription costs and billing cycles
- **🎨 Modern UI/UX** - Material Design 3 with dark/light themes
- **🔔 Smart Notifications** - Alerts for unused subscriptions
- **📱 Responsive Design** - Optimized for all Android devices

### 🚀 **Planned Features**
- **🤖 AI-Powered Recommendations** - Smart suggestions to save money
- **⏰ Scheduled Cancellations** - Set exact dates and times for cancellations
- **📈 Advanced Analytics** - Spending insights and usage trends
- **🔄 Automatic Detection** - Find subscriptions from installed apps
- **💰 One-Click Cancellation** - Direct integration with subscription services
- **📊 Export Data** - Download your subscription data

## 💰 **Pricing**

- **🎁 7-Day Free Trial** - Full access to all features
- **💳 Premium Plan** - Only $0.99/month after trial
- **🚀 Better Value** - Cheaper than competitors while offering more features

## 🏗️ **Architecture**

Built with modern Android development best practices:

- **🏛️ MVVM Architecture** - Clean separation of concerns
- **🗄️ Room Database** - Local data persistence
- **🔥 Firebase Integration** - Authentication and cloud sync
- **💉 Dependency Injection** - Hilt for scalable code
- **🎯 Reactive Programming** - Kotlin Coroutines and Flow
- **🎨 Jetpack Compose Ready** - Modern UI toolkit integration

## 🛠️ **Tech Stack**

### **Core Technologies**
- **Kotlin** - Primary development language
- **Android SDK** - Target API 34, Min API 24
- **Material Design 3** - Modern UI components

### **Architecture & Libraries**
- **Room Database** - Local data storage
- **Firebase Auth** - User authentication
- **Firebase Firestore** - Cloud database
- **Hilt** - Dependency injection
- **Navigation Component** - Fragment navigation
- **ViewBinding** - Type-safe view references
- **Coroutines & Flow** - Asynchronous programming

### **UI & Design**
- **Material Design 3** - Latest design system
- **Edge-to-Edge Experience** - Modern Android UI
- **Dark/Light Themes** - System theme support
- **Custom Animations** - Smooth user experience

## 📊 **App Structure**

```
Subscription Remover/
├── 🔐 Authentication
│   ├── Splash Screen
│   ├── Login/Register
│   └── Password Reset
├── 🏠 Home Dashboard
│   ├── Welcome & Greeting
│   ├── Quick Stats Cards
│   ├── Recent Subscriptions
│   ├── Upcoming Bills
│   └── Smart Recommendations
├── 📱 Subscriptions
│   ├── All Subscriptions
│   ├── Entertainment
│   ├── Social Media
│   ├── Productivity
│   ├── Fitness & Health
│   ├── News & Magazines
│   └── Miscellaneous
├── 📊 Analytics
│   ├── Spending Overview
│   ├── Category Breakdown
│   └── Usage Trends
└── ⚙️ Settings
    ├── Account Management
    ├── Notification Preferences
    └── App Preferences
```

## 🚦 **Getting Started**

### **Prerequisites**
- Android Studio Arctic Fox or newer
- Android SDK 24+
- Firebase project setup
- Git installed

### **Installation**

1. **Clone the repository**
```bash
git clone https://github.com/manngobeh2006/Subscription_Remover.git
cd Subscription_Remover
```

2. **Open in Android Studio**
- Open Android Studio
- Select "Open an existing project"
- Navigate to the cloned directory

3. **Firebase Setup**
- Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
- Add your Android app to the project
- Download `google-services.json` and place it in the `app/` directory
- Enable Authentication and Firestore in Firebase Console

4. **Build and Run**
```bash
./gradlew assembleDebug
```

### **Configuration**

Create a `local.properties` file in the root directory:
```properties
sdk.dir=/path/to/your/Android/Sdk
```

## 🎯 **Competitive Advantages**

### **vs Rocket Money & Other Competitors:**

| Feature | Subscription Remover | Rocket Money | Other Apps |
|---------|---------------------|--------------|------------|
| **Pricing** | $0.99/month | $3-6/month | $2-5/month |
| **Free Trial** | 7 days full access | Limited features | 3-7 days |
| **Category Organization** | ✅ Smart categories | ❌ Long lists | ⚠️ Basic |
| **Batch Operations** | ✅ Multi-select actions | ❌ One by one | ❌ Limited |
| **Scheduled Cancellation** | ✅ Exact date/time | ⚠️ Basic | ❌ Manual |
| **App Usage Tracking** | ✅ Real usage data | ❌ No tracking | ⚠️ Limited |
| **UI/UX Quality** | ✅ Modern Material 3 | ⚠️ Outdated | ⚠️ Basic |
| **Performance** | ✅ Native Android | ❌ Web-based | ⚠️ Mixed |

## 📈 **Roadmap**

### **Phase 1: Core Features** ✅
- [x] Authentication system
- [x] Basic UI/UX framework
- [x] Database structure
- [x] Category organization

### **Phase 2: Smart Features** 🚧
- [ ] Usage tracking implementation
- [ ] Smart notifications
- [ ] Recommendation engine
- [ ] Payment integration

### **Phase 3: Advanced Features** 📋
- [ ] Direct cancellation APIs
- [ ] Bank integration
- [ ] Advanced analytics
- [ ] Export functionality

### **Phase 4: Polish & Launch** 🚀
- [ ] Performance optimization
- [ ] Beta testing
- [ ] Play Store submission
- [ ] Marketing materials

## 🤝 **Contributing**

We welcome contributions! Here's how you can help:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit your changes** (`git commit -m 'Add some AmazingFeature'`)
4. **Push to the branch** (`git push origin feature/AmazingFeature`)
5. **Open a Pull Request**

### **Development Guidelines**
- Follow [Android development best practices](https://developer.android.com/guide)
- Use [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Write tests for new features
- Update documentation as needed

## 📝 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 **Support**

- **📧 Email**: support@subscriptionremover.com
- **🐛 Issues**: [GitHub Issues](https://github.com/manngobeh2006/Subscription_Remover/issues)
- **💬 Discussions**: [GitHub Discussions](https://github.com/manngobeh2006/Subscription_Remover/discussions)

## 🙏 **Acknowledgments**

- **Material Design 3** - Google's design system
- **Firebase** - Backend services
- **Android Jetpack** - Modern Android development
- **Kotlin Coroutines** - Asynchronous programming

---

**Made with ❤️ by [Emmanuel Ngobeh](https://github.com/manngobeh2006)**

*Take control of your subscriptions and start saving money today!*
