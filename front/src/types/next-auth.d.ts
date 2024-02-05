import { Account, DefaultSession } from 'next-auth';
import { JwtResponse, MemberWithToken } from '@/model/member';

declare module 'next-auth' {
  interface Account {
    userResponse: JwtResponse;
  }

  interface Session extends DefaultSession {
    user: MemberWithToken;
  }
}

declare module 'next-auth/jwt' {
  interface JWT {
    refreshToken: string;
    user: MemberWithToken;
  }
}
