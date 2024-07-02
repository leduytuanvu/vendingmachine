Vending Machine Project Documentation

Table of Contents
1. Introduction
2. Prerequisites
3. Setup Instructions
4. Architecture
5. Usage
6. Troubleshooting
7. Contributing
8. License
 
Introduction
The Vending Machine Project is an Android application developed using Jetpack Compose. It communicates with a physical vending machine via the RS232 serial communication protocol. This project showcases modern Android development practices, including the use of Jetpack Compose for UI, Kotlin Coroutines for asynchronous operations, and dependency injection with Hilt.

Prerequisites
Before you begin, ensure you have met the following requirements:
- Android Studio (version Arctic Fox or later)
- Android SDK (API level 21 or higher)
- A physical vending machine with RS232 serial communication capability
- RS232 to USB adapter (if your development machine does not have an RS232 port)
  
Setup Instructions
1. Clone the repository
git clone https://github.com/yourusername/vending-machine.git
cd vending-machine
2. Open the project in Android Studio
Launch Android Studio.
Select Open an existing Android Studio project.
Navigate to the project directory and select it.
3. Configure the Serial Port
Open SerialPortConfig.kt and configure the serial port parameters (e.g., baud rate, data bits, stop bits, parity).
object SerialPortConfig {
    const val BAUD_RATE = 9600
    const val DATA_BITS = 8
    const val STOP_BITS = 1
    const val PARITY = 0 // 0 = None, 1 = Odd, 2 = Even
    const val PORT_NAME = "/dev/ttyS0" // Update this to your port
}
4. Build and Run
Connect your Android device or start an emulator.
Click Run in Android Studio.

Architecture
The project follows the MVVM (Model-View-ViewModel) architecture pattern, ensuring a clean separation of concerns and making the codebase more maintainable.
- Components
UI Layer: Composed using Jetpack Compose.
ViewModel Layer: Handles UI-related data and business logic.
Model Layer: Represents the vending machine's data and operations.
Serial Communication: Manages the RS232 communication with the vending machine.
- Key Files
MainActivity.kt: Entry point of the application.
VendingViewModel.kt: ViewModel managing the vending machine's state and operations.
SerialPortManager.kt: Manages the RS232 serial communication.
VendingMachineRepository.kt: Repository handling data operations.
- Usage
Basic Operations
Connect to the Vending Machine

Ensure the RS232 connection is properly set up.
The app will automatically attempt to connect to the vending machine on launch.
Dispense an Item

Select an item from the UI and click the Dispense button.
The ViewModel will send the dispense command to the vending machine via the SerialPortManager.
Example Code
kotlin
Sao chép mã
@Composable
fun VendingMachineScreen(viewModel: VendingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        // Display items
        LazyColumn {
            items(uiState.items) { item ->
                Text(text = item.name)
                Button(onClick = { viewModel.dispenseItem(item) }) {
                    Text("Dispense")
                }
            }
        }

        // Display connection status
        Text(text = "Connection Status: ${uiState.connectionStatus}")
    }
}
Troubleshooting
Common Issues
Unable to Connect to the Vending Machine

Verify the serial port configuration in SerialPortConfig.kt.
Ensure the RS232 cable is properly connected.
Dispense Command Fails

Check the logcat for error messages.
Verify the command format and parameters.
Contributing
Contributions are welcome! Please fork the repository and create a pull request with your changes.

License
This project is licensed under the MIT License. See the LICENSE file for details.
