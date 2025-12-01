import { notificationsApi } from "./api";

type NotifyArgs = {
  destId?: number | string | null;
  title: string;
  type: string;
  message: string;
};

const getCurrentUser = () => {
  try {
    const raw = localStorage.getItem("jnb_user");
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
};

const getActorInfo = () => {
  const user = getCurrentUser();
  if (!user) return { id: null as number | null, name: null as string | null };
  const id = Number(user.utilisateurId) || null;
  const name = [user.prenom, user.nom].filter(Boolean).join(" ") || null;
  return { id, name };
};

export const smartNotify = async ({ destId, title, type, message }: NotifyArgs) => {
  const { id: actorId, name: actorName } = getActorInfo();
  const dest = typeof destId === "string" ? parseInt(destId, 10) : destId ?? null;

  if (!dest || dest <= 0) return;
  if (actorId && dest === actorId) return;

  const msg = actorName ? `${message}\n(Par ${actorName})` : message;
  try {
    await notificationsApi.create({
      destinataireId: dest,
      titre: title,
      type,
      message: msg,
    });
  } catch {
    // Ne bloque jamais le flow UX sur échec de notification
  }
};