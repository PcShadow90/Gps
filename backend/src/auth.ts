export type UserRole = "guest" | "rider" | "admin";

export interface RiderProfile {
  id: string;
  email: string;
  displayName: string;
  motorcycle?: string;
  avatarUrl?: string;
  role: UserRole;
  createdAt: string;
}