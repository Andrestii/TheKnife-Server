CREATE TABLE utenti (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    cognome VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    data_nascita DATE,
    domicilio VARCHAR(100),
    ruolo VARCHAR(20) CHECK (ruolo IN ('cliente', 'ristoratore')) NOT NULL
);

CREATE TABLE ristoranti (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    nazione VARCHAR(50),
    citta VARCHAR(50),
    indirizzo VARCHAR(100),
    latitudine DOUBLE PRECISION,
    longitudine DOUBLE PRECISION,
    prezzo INT CHECK (fascia_prezzo >= 0),
    delivery BOOLEAN DEFAULT FALSE,
    prenotazione BOOLEAN DEFAULT FALSE,
    tipo_cucina VARCHAR(50),
    id_ristoratore INT REFERENCES utenti(id) ON DELETE CASCADE
);

CREATE TABLE recensioni (
    id SERIAL PRIMARY KEY,
    id_ristorante INT REFERENCES ristoranti(id) ON DELETE CASCADE,
    id_utente INT REFERENCES utenti(id) ON DELETE CASCADE,
    stelle INT CHECK (stelle BETWEEN 1 AND 5),
    testo TEXT,
    risposta TEXT
);

CREATE TABLE preferiti (
    id_utente INT REFERENCES utenti(id) ON DELETE CASCADE,
    id_ristorante INT REFERENCES ristoranti(id) ON DELETE CASCADE,
    PRIMARY KEY (id_utente, id_ristorante)
);