#include <jni.h>
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include <unordered_map>
#include <string>
#include <dirent.h>
#include <vector>

static int serialPortVendingMachine = -1;
static int serialPortCashBox = -1;

extern "C" JNIEXPORT jint JNICALL
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_openPortVendingMachine(JNIEnv *env, jobject, jstring path, jstring portName, jint baudRate) {
    // Convert jstring to const char*
    const char *nativePath = env->GetStringUTFChars(path, JNI_FALSE);
    const char *nativePortName = env->GetStringUTFChars(portName, JNI_FALSE);
    if (nativePath == nullptr || nativePortName == nullptr) return -1; // OutOfMemoryError already thrown
    // Construct full path
    char fullPath[256];
    snprintf(fullPath, sizeof(fullPath), "%s%s", nativePath, nativePortName);
    // Open the serial port
    serialPortVendingMachine = open(fullPath, O_RDWR | 0);
    env->ReleaseStringUTFChars(path, nativePath);
    env->ReleaseStringUTFChars(portName, nativePortName);
    if (serialPortVendingMachine == -1) return -1;
    speed_t speed;
    switch (baudRate) {
        case 9600: speed = B9600; break;
            // Handle other baud rates as necessary
        default: speed = B9600; // Default to B9600 if no match
    }
    // Configure serial port settings
    struct termios tty{};
    tcgetattr(serialPortVendingMachine, &tty);
    cfmakeraw(&tty);
    cfsetispeed(&tty, speed);
    cfsetospeed(&tty, speed);
    // Further configure the tty
    tty.c_cflag &= ~(PARENB | CSTOPB | CSIZE);
    tty.c_cflag |= CS8 | CREAD | CLOCAL;
    tty.c_lflag &= ~(ECHO | ECHOE | ECHONL | ICANON | ISIG);
    tty.c_iflag &= ~(IXON | IXOFF | IXANY | IGNBRK | BRKINT | PARMRK | ISTRIP | INLCR | IGNCR | ICRNL);
    tty.c_oflag &= ~(OPOST | ONLCR);
    tty.c_cc[VTIME] = 0;
    tcsetattr(serialPortVendingMachine, TCSANOW, &tty);
    return 0;
}

extern "C" JNIEXPORT jint
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_writeDataPortVendingMachine(JNIEnv *env, jobject, jbyteArray data) {
if (serialPortVendingMachine == -1) return -1;
// Convert jbyteArray to a native byte array
jsize length = env->GetArrayLength(data);
jbyte *dataPtr = env->GetByteArrayElements(data, nullptr);
// Write data to the serial port
int bytesWritten = write(serialPortVendingMachine, dataPtr, length);
// Release the data array
env-> ReleaseByteArrayElements(data, dataPtr, JNI_ABORT);
return bytesWritten;
}

extern "C" JNIEXPORT void JNICALL
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_readDataPortVendingMachine(JNIEnv *env, jobject, jint bufferSize, jobject callback) {
if (serialPortVendingMachine == -1) return;
// Create a byte array to store the received data
jbyteArray data = env->NewByteArray(512);
// Read data from the serial port
jbyte *dataPtr = env->GetByteArrayElements(data, nullptr);
int bytesRead = read(serialPortVendingMachine, dataPtr, bufferSize);
if (bytesRead > 0) {
// Call the provided callback function with the received data
// Create a jbyteArray
// Create a jbyteArray to store the read data
jbyteArray dataArray = (*env).NewByteArray(bytesRead);
// Copy data from the buffer to the jbyteArray
(*env).SetByteArrayRegion(dataArray, 0, bytesRead, (jbyte *) dataPtr);
// Create a jbyteArray to store the bytesRead value
jbyteArray bytesReadArray = (*env).NewByteArray(sizeof(int));
// Copy the bytesRead value into the jbyteArray
auto *bytesReadBytes = (jbyte *) &bytesRead;
(*env).SetByteArrayRegion(bytesReadArray, 0, sizeof(int), bytesReadBytes);
data = dataArray;
jclass callbackClass = env->GetObjectClass(callback);
jmethodID callbackMethod = env->GetMethodID(callbackClass, "onDataReceivedVendingMachine", "([B)V");
env->CallVoidMethod(callback, callbackMethod, data);
env->ReleaseByteArrayElements(data, dataPtr, JNI_ABORT);
} else {
// If no data was read or there was an error, call the callback with an empty byte array
jclass callbackClass = env->GetObjectClass(callback);
jmethodID callbackMethod = env->GetMethodID(callbackClass, "onDataReceivedVendingMachine", "([B)V");
env->CallVoidMethod(callback, callbackMethod, data);
env->ReleaseByteArrayElements(data, dataPtr, 0);
}
}

extern "C" JNIEXPORT void JNICALL
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_closePortVendingMachine(JNIEnv *env, jobject) {
if (serialPortVendingMachine != -1) {
close(serialPortVendingMachine);
serialPortVendingMachine = -1;
}
}

extern "C" JNIEXPORT jint
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_openPortCashBox(JNIEnv *env, jobject, jstring path, jstring portName, jint baudRate) {
// Convert jstring to const char* for path and portName
const char *pathStr = env->GetStringUTFChars(path, nullptr);
const char *portNameStr = env->GetStringUTFChars(portName, nullptr);
// Construct full path by concatenating path and portName
std::string fullPathStr = std::string(pathStr) + std::string(portNameStr);
const char *fullPath = fullPathStr.c_str();
// Open the serial port
serialPortCashBox = open(fullPath, O_RDWR | O_NOCTTY | 0);
if (serialPortCashBox == -1) {
env->ReleaseStringUTFChars(path, pathStr);
env->ReleaseStringUTFChars(portName, portNameStr);
return -1; // Return -1 if unable to open port
}
// Configure serial port settings
struct termios tty{};
tcgetattr(serialPortCashBox, &tty);
speed_t speed;
switch (baudRate) {
case 9600: speed = B9600; break;
// Handle other baud rates as necessary
default: speed = B9600; // Default to B9600 if no match
}
cfsetispeed(&tty, speed);
cfsetospeed(&tty, speed);
// Enable and set even parity
tty.c_cflag |= PARENB;
tty.c_cflag &= ~PARODD;
tty.c_cflag &= ~CSTOPB;
tty.c_cflag &= ~CSIZE;
tty.c_cflag |= CS8;
tty.c_cflag |= CREAD | CLOCAL;
tty.c_lflag &= ~(ECHO | ECHOE | ECHONL | ICANON | ISIG);
tty.c_iflag &= ~(IXON | IXOFF | IXANY | IGNBRK | BRKINT | PARMRK | ISTRIP | INLCR | IGNCR | ICRNL);
tty.c_oflag &= ~(OPOST | ONLCR);
tty.c_cc[VTIME] = 0;
tcsetattr(serialPortCashBox, TCSANOW, &tty);
// Release the memory allocated by GetStringUTFChars
env->ReleaseStringUTFChars(path, pathStr);
env->ReleaseStringUTFChars(portName, portNameStr);
return 0; // Return 0 on success
}

extern "C" JNIEXPORT jint
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_writeDataPortCashBox(JNIEnv *env, jobject, jbyteArray data) {
if (serialPortCashBox == -1) return -1;
// Convert jbyteArray to a native byte array
jsize length = env->GetArrayLength(data);
jbyte *dataPtr = env->GetByteArrayElements(data, nullptr);
// Write data to the serial port
int bytesWritten = write(serialPortCashBox, dataPtr, length);
// Release the data array
env-> ReleaseByteArrayElements(data, dataPtr, JNI_ABORT);
return bytesWritten;
}

extern "C" JNIEXPORT void JNICALL
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_readDataPortCashBox(JNIEnv *env, jobject, jint bufferSize, jobject callback) {
if (serialPortCashBox == -1) return;
// Create a byte array to store the received data
jbyteArray data = env->NewByteArray(bufferSize);
// Read data from the serial port
jbyte *dataPtr = env->GetByteArrayElements(data, nullptr);
int bytesRead = read(serialPortCashBox, dataPtr, bufferSize);
if (bytesRead > 0) {
// Call the provided callback function with the received data
// Create a jbyteArray
// Create a jbyteArray to store the read data
jbyteArray dataArray = (*env).NewByteArray(bytesRead);
// Copy data from the buffer to the jbyteArray
(*env).SetByteArrayRegion(dataArray, 0, bytesRead, (jbyte *) dataPtr);
// Create a jbyteArray to store the bytesRead value
jbyteArray bytesReadArray = (*env).NewByteArray(sizeof(int));
// Copy the bytesRead value into the jbyteArray
auto *bytesReadBytes = (jbyte *) &bytesRead;
(*env).SetByteArrayRegion(bytesReadArray, 0, sizeof(int), bytesReadBytes);
data = dataArray;
jclass callbackClass = env->GetObjectClass(callback);
jmethodID callbackMethod = env->GetMethodID(callbackClass, "onDataReceivedCashBox", "([B)V");
env->CallVoidMethod(callback, callbackMethod, data);
env->ReleaseByteArrayElements(data, dataPtr, JNI_ABORT);
} else {
// If no data was read or there was an error, call the callback with an empty byte array
jclass callbackClass = env->GetObjectClass(callback);
jmethodID callbackMethod = env->GetMethodID(callbackClass, "onDataReceivedCashBox", "([B)V");
env->CallVoidMethod(callback, callbackMethod, data);
env->ReleaseByteArrayElements(data, dataPtr, 0);
}
}

extern "C" JNIEXPORT void JNICALL
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_closePortCashBox(JNIEnv *env, jobject) {
if (serialPortCashBox != -1) {
close(serialPortCashBox);
serialPortCashBox = -1;
}
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_getAllSerialPorts(JNIEnv *env, jobject) {
    std::vector<std::string> portNames;
    DIR *dir;
    struct dirent *ent;
    if ((dir = opendir("/dev")) != nullptr) {
        while ((ent = readdir(dir)) != nullptr) {
            std::string name = ent->d_name;
//            if (name.find("ttyS") == 0 || name.find("ttyUSB") == 0) {
            if (name.find("ttyS") == 0) {
                portNames.push_back(name);
            }
        }
        closedir(dir);
    } else {
        // Failed to open directory
        return nullptr;
    }
    jobjectArray result = env->NewObjectArray(portNames.size(), env->FindClass("java/lang/String"), nullptr);
    for (int i = 0; i < portNames.size(); i++) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(portNames[i].c_str()));
    }

    return result;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_leduytuanvu_vendingmachine_core_datasource_portConnectionDatasource_PortConnectionHelperDatasource_getAllSerialPortsStatus(JNIEnv *env, jobject) {
    std::vector<std::pair<jstring, jobject>> portStatuses;
    DIR *dir;
    struct dirent *ent;
    if ((dir = opendir("/dev")) != nullptr) {
        jclass booleanClass = env->FindClass("java/lang/Boolean");
        jmethodID booleanConstructor = env->GetMethodID(booleanClass, "<init>", "(Z)V");
        while ((ent = readdir(dir)) != nullptr) {
            std::string name = ent->d_name;
            if (name.find("ttyS") == 0) {
                std::string fullPath = "/dev/" + name;
                int fd = open(fullPath.c_str(), O_RDWR | O_NOCTTY);
                jstring portName = env->NewStringUTF(name.c_str());
                jobject booleanObject;
                if (fd == -1) {
                    booleanObject = env->NewObject(booleanClass, booleanConstructor, JNI_FALSE);
                } else {
                    booleanObject = env->NewObject(booleanClass, booleanConstructor, JNI_TRUE);
                    close(fd);
                }
                portStatuses.emplace_back(portName, booleanObject);
            }
        }
        closedir(dir);
    } else {
        return nullptr;
    }
    jclass pairClass = env->FindClass("android/util/Pair");
    jmethodID pairConstructor = env->GetMethodID(pairClass, "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V");
    jobjectArray result = env->NewObjectArray(portStatuses.size(), pairClass, nullptr);
    for (size_t i = 0; i < portStatuses.size(); i++) {
        jobject pair = env->NewObject(pairClass, pairConstructor, portStatuses[i].first, portStatuses[i].second);
        env->SetObjectArrayElement(result, i, pair);
    }
    return result;
}