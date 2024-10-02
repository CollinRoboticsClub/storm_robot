const API_SERVER_WS_URL = `ws://${location.host}/api`;
const API_ENDPOINT_WHEELS = `${API_SERVER_WS_URL}/wheels/move`;
const API_ENDPOINT_ARM = `${API_SERVER_WS_URL}/arm/move`;

// init joysticks
const joystickWheelsOptions = {
    internalFillColor: '#AA0000',
    internalStrokeColor: '#330000',
    internalLineWidth: 2,
    externalStrokeColor: '#800000',
    externalLineWidth: 2,
};
const joystickArmOptions = {
    internalFillColor: '#0000AA',
    internalStrokeColor: '#000033',
    internalLineWidth: 2,
    externalStrokeColor: '#000080',
    externalLineWidth: 2,
};
const joystickRobotPosition = new JoyStick(
    'joystickRobotPosition',
    'assets/cross-arrows-icon.webp',
    joystickWheelsOptions
);
const joystickRobotRotation = new JoyStick(
    'joystickRobotRotation',
    'assets/rotate-icon.webp',
    joystickWheelsOptions
);
const joystickArmBaseShoulder = new JoyStick(
    'joystickArmBaseShoulder',
    null,
    joystickArmOptions
);
const joystickArmElbowWrist = new JoyStick(
    'joystickArmElbowWrist',
    null,
    joystickArmOptions
);
const joystickArmGripper = new JoyStick(
    'joystickArmGripper',
    null,
    joystickArmOptions
);

const wsArm = new WebSocket(API_ENDPOINT_ARM);
const wsWheels = new WebSocket(API_ENDPOINT_WHEELS);

const sendWheelDataIntervalMS = 100;
const sendArmDataIntervalMS = 25;

wsWheels.onopen = (_) => {
    window.setInterval(function () {
        sendWheelData();
    }, sendWheelDataIntervalMS);
};
wsArm.onopen = (_) => {
    window.setInterval(function () {
        sendArmData();
    }, sendArmDataIntervalMS);
};

// Clamp value to [-100, 100] to work around bug of joysticks sometimes going out of their intended bounds
function clamp(number) {
    let min = -100;
    let max = 100;
    return Math.max(min, Math.min(number, max));
}

function sendWheelData() {
    // wheels
    let wheelX = Number(joystickRobotPosition.GetX());
    let wheelY = Number(joystickRobotPosition.GetY());
    let wheelRotationMagnitude = Number(joystickRobotRotation.GetX());
    //let unused = Number(joystickRobotRotation.GetY());

    let wheelData = {
        x: wheelX,
        y: wheelY,
        rotation: wheelRotationMagnitude,
    };

    for (const value in wheelData) {
        // clamp values to address bug mentioned above clamp() declaration
        wheelData[value] = clamp(wheelData[value]);

        // change wheel range from [-100, 100] to [-1, 1]
        wheelData[value] /= 100;
    }

    wsWheels.send(JSON.stringify(wheelData));
}

function sendArmData() {
    // arm
    let armBaseRotation = Number(joystickArmBaseShoulder.GetX());
    let armShoulderRotation = Number(joystickArmBaseShoulder.GetY());
    let armElbowRotation = Number(joystickArmElbowWrist.GetX());
    let armWristRotation = Number(joystickArmElbowWrist.GetY());
    let armGripperRotation = Number(joystickArmGripper.GetX());
    //let armUnusedRotation = Number(joystickArmGripper.GetY());

    let armData = {
        baseAngle: armBaseRotation,
        shoulderAngle: armShoulderRotation,
        elbowAngle: armElbowRotation,
        wristAngle: armWristRotation,
        gripperAngle: armGripperRotation,
    };

    let allZeroes = true;
    for (const servo in armData) {
        if (armData[servo] === 0) {
            continue;
        } else {
            allZeroes = false;
        }

        // clamp values to address bug mentioned above clamp() declaration
        armData[servo] = clamp(armData[servo]);

        // TODO:
        // - check if this speed is fine, if not, adjust it and delete this TODO
        // - move speed control into the backend, because frontend code shouldn't be able to control that
        //
        // Since these are *relative* movements (change in degrees) that we're sending, and we send them pretty
        // quickly, we wouldn't want to move the servos *too* quickly, so:
        // change arm range from [-100, 100] to [-2, 2]
        armData[servo] /= 50;

        // turn into int
        armData[servo] = Math.trunc(armData[servo]);
    }

    if (!allZeroes) {
        wsArm.send(JSON.stringify(armData));
    }
}
