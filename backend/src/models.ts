export interface Report{id:string;type:string;description?:string;lat:number;lon:number;createdAt:string;expiresAt?:string}
export interface Poi{id:string;type:string;name:string;lat:number;lon:number;metadata?:Record<string,unknown>}
export interface Group{id:string;name:string;ownerId?:string;memberCount:number}
