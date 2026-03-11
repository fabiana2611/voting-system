"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams, useSearchParams } from "next/navigation";

type SessionResponse = {
  sessionId: string;
  pautaId: string;
  pautaName: string;
  openedAt: string;
  closesAt: string;
  closed: boolean;
  yesCount: number;
  noCount: number;
};

export default function PautaSessionPage() {
  const params = useParams<{ id: string }>();
  const searchParams = useSearchParams();
  const sessionId = searchParams.get("sessionId");
  const [session, setSession] = useState<SessionResponse | null>(null);
  const [status, setStatus] = useState<"idle" | "loading" | "error">("idle");
  const [error, setError] = useState<string | null>(null);
  const [shareUrl, setShareUrl] = useState<string>("");

  const pautaId = params.id;

  const sessionUrl = useMemo(() => {
    if (!sessionId) {
      return "";
    }
    return `/pautas/${pautaId}/vote?sessionId=${sessionId}`;
  }, [pautaId, sessionId]);

  useEffect(() => {
    if (!sessionId) {
      setError("Sessão inválida");
      return;
    }

    let isMounted = true;
    let intervalId: NodeJS.Timeout | null = null;

    const loadSession = async () => {
      setStatus("loading");
      setError(null);

      try {
        const response = await fetch(
          `http://localhost:8080/api/v1/pautas/${pautaId}/sessions/${sessionId}`,
          { method: "GET" }
        );

        if (!response.ok) {
          throw new Error("Falha ao carregar sessão");
        }

        const data = (await response.json()) as SessionResponse;
        if (isMounted) {
          setSession(data);
          setStatus("idle");
        }
      } catch (err) {
        if (isMounted) {
          setStatus("error");
          setError(err instanceof Error ? err.message : "Erro inesperado");
        }
      }
    };

    loadSession();
    intervalId = setInterval(loadSession, 2000);

    return () => {
      isMounted = false;
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [pautaId, sessionId]);

  useEffect(() => {
    if (sessionUrl) {
      setShareUrl(`${window.location.origin}${sessionUrl}`);
    }
  }, [sessionUrl]);

  return (
    <main className="page">
      <section className="card card-wide">
        <div className="row">
          <h1 className="title">Sessão da pauta</h1>
          <a href="/pautas" className="button button-secondary">
            Voltar
          </a>
        </div>

        {status === "error" && error && (
          <div className="alert error spacing-top">{error}</div>
        )}

        {status === "loading" && !session && (
          <p className="muted spacing-top">Carregando sessão...</p>
        )}

        {session && (
          <div className="stack">
            <div>
              <p className="muted">Pauta</p>
              <p className="title-sm">{session.pautaName}</p>
            </div>

            <div>
              <p className="muted">URL para compartilhar</p>
              <div className="row spacing-top-sm">
                <input type="text" value={shareUrl} readOnly className="input" />
                <a href={sessionUrl} className="button button-secondary">
                  Abrir
                </a>
              </div>
            </div>

            <div className="alert">
              <p className="muted">Parciais</p>
              <div className="actions spacing-top">
                <span className="pill">Sim: {session.yesCount}</span>
                <span className="pill">Não: {session.noCount}</span>
              </div>
            </div>

            {session.closed ? (
              <div className="alert success">
                Sessão finalizada. Resultado: {session.yesCount} Sim / {session.noCount} Não.
              </div>
            ) : (
              <div className="alert">Sessão aberta. Aguardando votos...</div>
            )}
          </div>
        )}
      </section>
    </main>
  );
}
