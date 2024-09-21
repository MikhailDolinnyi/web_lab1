class InvalidValueException extends Error {
  constructor(message) {
    super(message);
    this.name = "InvalidValueException";
  }
}

function validateFormInput(values) {
  if (values.x === undefined) {
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

  const formData = new FormData(this);
  /* @type FormValues */
  const values = Object.fromEntries(formData);



  try {
    validateFormInput(values);
    errorDiv.hidden = true;
  } catch (e) {
    errorDiv.hidden = false;
    errorDiv.textContent = e.message;
    return;
  }




const params = new URLSearchParams(formData);
const url = "/fcgi-bin/lab-1.jar?" + params.toString();

const response = await fetch(url);

const newRow = table.insertRow(-1);

const rowX = newRow.insertCell(0);
const rowY = newRow.insertCell(1);
const rowR = newRow.insertCell(2);
const rowResult = newRow.insertCell(3);

rowX.textContent = values.x;
rowY.textContent = values.y;
rowR.textContent = values.r;

if (response.ok) {
  /** @type {{result: boolean}} */
  const result = await response.json();
  rowResult.textContent = result.result.toString();
} else {
  /** @type {{reason: string}} */
  const result = await response.json();
  rowResult.textContent = "error";
  console.error(result);}
}


const form = document.getElementById("data-form");
form.addEventListener('submit', onSubmit);
