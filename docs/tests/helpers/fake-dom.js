class FakeElement {
    constructor() {
        this.attributes = new Map();
        this.disabled = false;
        this.href = '';
        this.innerHTML = '';
        this.innerText = '';
        this.listeners = new Map();
        this.style = {};
        this.textContent = '';
        this.value = '';
    }

    addEventListener(type, listener) {
        this.listeners.set(type, listener);
    }

    removeAttribute(name) {
        this.attributes.delete(name);
    }

    setAttribute(name, value) {
        this.attributes.set(name, String(value));
    }
}

function createDocument(initialValues = {}) {
    const elements = new Map();

    return {
        cookie: '',
        elements,
        getElementById(id) {
            if (!elements.has(id)) {
                const element = new FakeElement();
                if (Object.hasOwn(initialValues, id)) element.value = initialValues[id];
                elements.set(id, element);
            }
            return elements.get(id);
        },
    };
}

module.exports = { FakeElement, createDocument };
