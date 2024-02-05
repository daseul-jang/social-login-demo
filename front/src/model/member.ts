export interface Member {
  id: number;
  email: string;
  name: string | null;
  nickname: string | null;
  profileImage: string | null;
  provider: string;
}

export interface MemberWithToken extends Member {
  token: {
    accessToken: string;
    accessTokenExp: number;
  };
}

export interface JwtResponse {
  accessToken: string;
  accessTokenExp: number;
  refreshToken: string;
  member: Member;
}
