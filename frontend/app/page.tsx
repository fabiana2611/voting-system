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
        const response = await fetch("http://localhost:8080/api/hello", {
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
    <main className="min-h-screen bg-white text-slate-900 flex items-center justify-center p-6">
      <section className="max-w-xl w-full rounded-2xl border border-slate-200 shadow-sm p-8 text-center">
        <h1 className="text-3xl font-semibold">Olá, mundo</h1>
        <p className="mt-4 text-lg text-slate-700">
          {error ? `Erro: ${error}` : message}
        </p>
      </section>
    </main>
  );
}
