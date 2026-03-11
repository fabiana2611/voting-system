"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export default function NewPautaPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [status, setStatus] = useState<"idle" | "saving" | "success" | "error">("idle");
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setStatus("saving");
    setError(null);

    try {
      const response = await fetch("http://localhost:8080/api/v1/pautas", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ name })
      });

      if (!response.ok) {
        throw new Error("Falha ao cadastrar a pauta");
      }

      setStatus("success");
      setName("");
      router.push("/");
    } catch (err) {
      setStatus("error");
      setError(err instanceof Error ? err.message : "Erro inesperado");
    }
  };

  return (
    <main className="page">
      <section className="card">
        <h1 className="title">Cadastrar nova pauta</h1>
        <p className="subtitle">Informe o nome da pauta e envie para o backend.</p>

        <form className="stack" onSubmit={handleSubmit}>
          <label className="label" htmlFor="pauta-name">
            Nome da pauta
          </label>
          <input
            id="pauta-name"
            name="pauta-name"
            type="text"
            value={name}
            onChange={(event) => setName(event.target.value)}
            className="input"
            placeholder="Ex: Aprovação do orçamento"
            required
          />

          <div className="actions">
            <button
              type="submit"
              disabled={status === "saving" || name.trim().length === 0}
              className="button button-primary"
            >
              {status === "saving" ? "Salvando..." : "Enviar"}
            </button>
            <a href="/" className="button button-secondary">
              Cancelar
            </a>
          </div>

          {status === "success" && (
            <div className="alert success">Pauta cadastrada com sucesso.</div>
          )}
          {status === "error" && error && (
            <div className="alert error">{error}</div>
          )}
        </form>
      </section>
    </main>
  );
}
