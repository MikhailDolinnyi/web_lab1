class InvalidValueException extends Error {
    constructor(message) {
        super(message);
        this.name = "InvalidValueException";
    }
}

function validateFormInput(values) {
    if (values.x === null) {
        throw new InvalidValueException("Пожалуйста, выберите X");
    }

    if (isNaN(values.y)) {
        throw new InvalidValueException("Неверное значение Y");
    }

    const y = parseInt(values.y);
    if (y < -5 || y > 5) {
        throw new InvalidValueException("Число Y не входит в диапазон")
    }

    if (isNaN(values.r)) {
        throw new InvalidValueException("Неверное значение радиуса")
    }
}


/** @type HTMLTableElement */
const table = document.getElementById("result-table");

/** @type HTMLDivElement */
const errorDiv = document.getElementById("error");

async function onSubmit(ev) {
    ev.preventDefault();
    document.getElementById("audio").play();
    const formData = new FormData(form);
    const values = {
        x: formData.get('x'),
        y: formData.get('y'),
        r: formData.get('r')
    };

    try {
        validateFormInput(values);
        errorDiv.hidden = true;
    } catch (e) {
        errorDiv.hidden = false;
        errorDiv.textContent = e.message;
        return
    }


    const response = await fetch('/fcgi-bin/lab-1.jar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(values)
    });


    const newRow = table.insertRow(-1);
    const rowX = newRow.insertCell(0);
    const rowY = newRow.insertCell(1);
    const rowR = newRow.insertCell(2);
    const rowTime = newRow.insertCell(3);
    const rowNow = newRow.insertCell(4);
    const rowResult = newRow.insertCell(5);

    const y = parseFloat(values.y).toFixed(2);

    rowX.textContent = values.x;
    rowY.textContent = y;
    rowR.textContent = values.r;

    if (response.ok) {
        const result = await response.json();
        rowTime.textContent = result.time;
        rowNow.textContent = result.now;
        rowResult.style.color = "green";
        rowResult.textContent = result.result.toString();
        // localStorage.setItem('x', values.x);
        // localStorage.setItem('y', y);
        // localStorage.setItem('x', values.r);

    } else {
        const result = await response.json();
        rowResult.style.color = "red";
        rowResult.textContent = "error";
        rowNow.textContent = result.now;
        console.error(result);
    }


}


const form = document.getElementById("data-form");
form.addEventListener('submit', onSubmit);


// let recover_x = localStorage.getItem()