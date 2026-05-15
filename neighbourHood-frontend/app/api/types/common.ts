export interface loginParams {
    email: string;
    password: string;
}

export interface registerParams {
    name: string;
    age: string | number;
    hkid: string;
    address1: string;
    address2: string;
    address3?: string;
    phone: string;
    email: string;
    password: string;
    status?: string;
}

export interface BasicResponse<T> {
    code: string;
    success: boolean;
    data: T;
    message: string;
}

export interface User {
    uuid: String;
    username: String;
    email: String;
    house: String;
}

export interface PublicUserProfile {
    uuid: string;
    username: string;
    email: string;
    status?: string;
    house?: string;
    address1?: string;
    address2?: string;
    address3?: string;
}