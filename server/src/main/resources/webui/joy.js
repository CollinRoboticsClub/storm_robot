/*
 * Name          : joy.js
 * @author       : Roberto D'Amico (Bobboteck)
 * Revision      : 1.1.6, modified by Arian Baishya
 *
 * The MIT License (MIT)
 *
 *  This file is part of the JoyStick Project (https://github.com/bobboteck/JoyStick).
 *	Copyright (c) 2015 Roberto D'Amico (Bobboteck).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * @desc Principal object that draws a joystick, you only need to initialize the object and suggest the HTML container
 * @costructor
 * @param container {String} - HTML object that contains the Joystick
 * @param parameters (optional) - object with following keys:
 *  title {String} (optional) - The ID of canvas (Default value is 'joystick')
 *  width {Int} (optional) - The width of canvas, if not specified is set at width of container object (Default value is the width of container object)
 *  height {Int} (optional) - The height of canvas, if not specified is set at height of container object (Default value is the height of container object)
 *  internalFillColor {String} (optional) - Internal color of Stick (Default value is '#00AA00')
 *  internalLineWidth {Int} (optional) - Border width of Stick (Default value is 2)
 *  internalStrokeColor {String}(optional) - Border color of Stick (Default value is '#003300')
 *  externalLineWidth {Int} (optional) - External reference circumference width (Default value is 2)
 *  externalStrokeColor {String} (optional) - External reference circumference color (Default value is '#008000')
 *  autoReturnToCenter {Bool} (optional) - Sets the behavior of the stick, whether or not, it should return to zero position when released (Default value is True and return to zero)
 */
const JoyStick = (function (container, imageSrc, parameters) {
    let image;
    if (typeof imageSrc === "undefined" || imageSrc === null) {
        image = null;
        imageSrc = null;
    } else {
        image = new Image();
        image.src = imageSrc;
    }
    parameters = parameters || {};
    let width = (typeof parameters.width === "undefined" ? 0 : parameters.width),
        height = (typeof parameters.height === "undefined" ? 0 : parameters.height);
    const title = (typeof parameters.title === "undefined" ? "joystick" : parameters.title),
        internalFillColor = (typeof parameters.internalFillColor === "undefined" ? "#00AA00" : parameters.internalFillColor),
        internalLineWidth = (typeof parameters.internalLineWidth === "undefined" ? 2 : parameters.internalLineWidth),
        internalStrokeColor = (typeof parameters.internalStrokeColor === "undefined" ? "#003300" : parameters.internalStrokeColor),
        externalLineWidth = (typeof parameters.externalLineWidth === "undefined" ? 2 : parameters.externalLineWidth),
        externalStrokeColor = (typeof parameters.externalStrokeColor === "undefined" ? "#008000" : parameters.externalStrokeColor),
        autoReturnToCenter = (typeof parameters.autoReturnToCenter === "undefined" ? true : parameters.autoReturnToCenter);

    // Create Canvas element and add it in the Container object
    const objContainer = document.getElementById(container);

    // Fixing Unable to preventDefault inside passive event listener due to target being treated as passive in Chrome [Thanks to https://github.com/artisticfox8 for this suggestion]
    objContainer.style.touchAction = "none";

    const canvas = document.createElement("canvas");
    canvas.id = title;
    if (width === 0) {
        width = objContainer.clientWidth;
    }
    if (height === 0) {
        height = objContainer.clientHeight;
    }
    canvas.width = width;
    canvas.height = height;
    objContainer.appendChild(canvas);
    const context = canvas.getContext("2d");

    // Set flag and force re-draw once image is loaded
    let imageIsLoaded = false;
    if (image !== null) {
        image.onload = function () {
            imageIsLoaded = true;
            drawInternal();
        };
    }

    let pressed = 0; // Bool - 1=Yes - 0=No
    const circumference = 2 * Math.PI;
    const internalRadius = (canvas.width - ((canvas.width / 2) + 10)) / 2;
    const maxMoveStick = internalRadius + 5;
    const externalRadius = internalRadius + 30;
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const directionHorizontalLimitPos = canvas.width / 10;
    const directionHorizontalLimitNeg = directionHorizontalLimitPos * -1;
    const directionVerticalLimitPos = canvas.height / 10;
    const directionVerticalLimitNeg = directionVerticalLimitPos * -1;

    // Used to save current position of stick
    let movedX = centerX;
    let movedY = centerY;

    // Check if the device support the touch or not
    if ("ontouchstart" in document.documentElement) {
        canvas.addEventListener("touchstart", onTouchStart, false);
        document.addEventListener("touchmove", onTouchMove, false);
        document.addEventListener("touchend", onTouchEnd, false);
    } else {
        canvas.addEventListener("mousedown", onMouseDown, false);
        document.addEventListener("mousemove", onMouseMove, false);
        document.addEventListener("mouseup", onMouseUp, false);
    }
    // Draw the object
    drawExternal();
    drawInternal();

    /******************************************************
     * Private methods
     *****************************************************/

    /**
     * @desc Draw the external circle used as reference position
     */
    function drawExternal() {
        context.beginPath();
        context.arc(centerX, centerY, externalRadius, 0, circumference, false);
        context.lineWidth = externalLineWidth;
        context.strokeStyle = externalStrokeColor;
        context.stroke();
    }

    /**
     * @desc Draw the internal stick in the current position the user have moved it
     */
    function drawInternal() {
        context.beginPath();
        if (movedX < internalRadius) {
            movedX = maxMoveStick;
        }
        if ((movedX + internalRadius) > canvas.width) {
            movedX = canvas.width - (maxMoveStick);
        }
        if (movedY < internalRadius) {
            movedY = maxMoveStick;
        }
        if ((movedY + internalRadius) > canvas.height) {
            movedY = canvas.height - (maxMoveStick);
        }
        context.arc(movedX, movedY, internalRadius, 0, circumference, false);
        // create radial gradient
        const grd = context.createRadialGradient(centerX, centerY, 5, centerX, centerY, 200);
        // Light color
        grd.addColorStop(0, internalFillColor);
        // Dark color
        grd.addColorStop(1, internalStrokeColor);
        context.fillStyle = grd;
        context.fill();
        context.lineWidth = internalLineWidth;
        context.strokeStyle = internalStrokeColor;
        context.stroke();

        if (image !== null && imageIsLoaded) {
            let imageHeight = internalRadius;
            let imageWidth = internalRadius;
            let dx = movedX - (imageWidth / 2);
            let dy = movedY - (imageHeight / 2);
            context.drawImage(image, dx, dy, imageWidth, imageHeight)
        }
    }

    /**
     * @desc Events for manage touch
     */
    let touchId = null;

    function onTouchStart(event) {
        pressed = 1;
        touchId = event.targetTouches[0].identifier;
    }

    function onTouchMove(event) {
        if (pressed === 1 && event.targetTouches[0].target === canvas) {
            movedX = event.targetTouches[0].pageX;
            movedY = event.targetTouches[0].pageY;

            // Manage offset
            onMove()
        }
    }

    function onTouchEnd(event) {
        if (event.changedTouches[0].identifier !== touchId) return;

        onMoveEnd()
    }

    /**
     * @desc Events for manage mouse
     */
    function onMouseDown(_) {
        pressed = 1;
    }

    /*
    To simplify this code there was a new experimental feature here: https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/offsetX
    but it's present only in Mouse case not in Touch case :-(
    */
    function onMouseMove(event) {
        if (pressed === 1) {
            movedX = event.pageX;
            movedY = event.pageY;

            // Manage offset
            onMove()
        }
    }

    function onMouseUp(_) {
        onMoveEnd()
    }

    function onMove() {
        if (canvas.offsetParent.tagName.toUpperCase() === "BODY") {
            movedX -= canvas.offsetLeft;
            movedY -= canvas.offsetTop;
        } else {
            movedX -= canvas.offsetParent.offsetLeft;
            movedY -= canvas.offsetParent.offsetTop;
        }

        // Delete canvas
        deleteCanvas()
    }

    function onMoveEnd() {
        pressed = 0;
        // If required reset position store variable
        if (autoReturnToCenter) {
            movedX = centerX;
            movedY = centerY;
        }

        deleteCanvas()
    }

    function deleteCanvas() {
        context.clearRect(0, 0, canvas.width, canvas.height);

        // Redraw object
        drawExternal();
        drawInternal();
    }

    function getCardinalDirection() {
        let result = "";
        let horizontal = movedX - centerX;
        let vertical = movedY - centerY;

        if (vertical >= directionVerticalLimitNeg && vertical <= directionVerticalLimitPos) {
            result = "C";
        }
        if (vertical < directionVerticalLimitNeg) {
            result = "N";
        }
        if (vertical > directionVerticalLimitPos) {
            result = "S";
        }

        if (horizontal < directionHorizontalLimitNeg) {
            if (result === "C") {
                result = "W";
            } else {
                result += "W";
            }
        }
        if (horizontal > directionHorizontalLimitPos) {
            if (result === "C") {
                result = "E";
            } else {
                result += "E";
            }
        }

        return result;
    }

    /******************************************************
     * Public methods
     *****************************************************/

    /**
     * @desc The width of canvas
     * @return Number of pixel width
     */
    this.GetWidth = function () {
        return canvas.width;
    };

    /**
     * @desc The height of canvas
     * @return Number of pixel height
     */
    this.GetHeight = function () {
        return canvas.height;
    };

    /**
     * @desc The X position of the cursor relative to the canvas that contains it and to its dimensions
     * @return Number that indicate relative position
     */
    this.GetPosX = function () {
        return movedX;
    };

    /**
     * @desc The Y position of the cursor relative to the canvas that contains it and to its dimensions
     * @return Number that indicate relative position
     */
    this.GetPosY = function () {
        return movedY;
    };

    /**
     * @desc Normalized value of X move of stick
     * @return Integer from -100 to +100
     */
    this.GetX = function () {
        return (100 * ((movedX - centerX) / maxMoveStick)).toFixed();
    };

    /**
     * @desc Normalized value of Y move of stick
     * @return Integer from -100 to +100
     */
    this.GetY = function () {
        return ((100 * ((movedY - centerY) / maxMoveStick)) * -1).toFixed();
    };

    /**
     * @desc Get the direction of the cursor as a string that indicates the cardinal points where this is oriented
     * @return String of cardinal point N, NE, E, SE, S, SW, W, NW and C when it is placed in the center
     */
    this.GetDir = function () {
        return getCardinalDirection();
    };
});
