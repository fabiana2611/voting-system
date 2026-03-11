"use client";

import { useEffect, useState } from "react";

type HelloResponse = {
  message: string;
};

export default function HomePage() {
  const [message, setMessage] = useState<string>("Carregando...");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadMessage = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/v1/hello", {
          method: "GET"
        });

        if (!response.ok) {
          throw new Error("Falha ao buscar a mensagem");
        }

        const data = (await response.json()) as HelloResponse;

        if (isMounted) {
          setMessage(data.message);
        }
      } catch (err) {
        if (isMounted) {
          setError(err instanceof Error ? err.message : "Erro inesperado");
        }
      }
    };

    loadMessage();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <main className="page">
      <section className="card">
        <h1 className="title">Olá, mundo</h1>
        <p className="subtitle">
          {error ? `Erro: ${error}` : message}
        </p>
        <div className="actions center spacing-top">
          <a href="/pautas/new" className="button button-primary">
            Cadastrar nova pauta
          </a>
          <a href="/pautas" className="button button-secondary">
            Listar Pautas
          </a>
        </div>
      </section>
    </main>
  );
}
