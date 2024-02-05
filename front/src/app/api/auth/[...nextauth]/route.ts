import { JwtResponse } from '@/model/member';
import { NextAuthOptions } from 'next-auth';
import NextAuth from 'next-auth/next';
import KakaoProvider from 'next-auth/providers/kakao';

export const authOptions: NextAuthOptions = {
  providers: [
    KakaoProvider({
      clientId: process.env.KAKAO_CLIENT_ID || '',
      clientSecret: process.env.KAKAO_CLIENT_SECRET || '',
    }),
  ],
  callbacks: {
    async signIn({ user, account, profile }) {
      console.log('signIn account');
      console.log(account);

      try {
        const res = await fetch(
          `http://localhost:8080/api/login/${account?.provider}`,
          {
            method: 'POST',
            body: account?.access_token,
          }
        );

        if (!(res.status >= 200 && res.status < 400)) {
          return false;
        }

        const data = await res.json();

        account!!.userResponse = data;

        return true;
      } catch (err) {
        console.log(err);

        return false;
      }
    },
    async jwt({ token, user, account, profile }) {
      console.log('jwt account');
      console.log(account);

      if (account) {
        token.refreshToken = account.userResponse.refreshToken;
        token.user = {
          ...account.userResponse.member,
          token: {
            accessToken: account.userResponse.accessToken,
            accessTokenExp: account.userResponse.accessTokenExp,
          },
        };
      }

      console.log('jwt token');
      console.log(token);

      /**
       * 리프레시 토큰 이용해서 액세스 토큰 재발급하는 로직 작성하는 곳
       */

      return token;
    },
    async session({ session, token, user }) {
      console.log('session token');
      console.log(token.user);

      if (token.user) {
        session.user = token.user;
      }

      console.log('session info');
      console.log(session);

      return session;
    },
  },
  pages: {
    signIn: '/',
  },
};

const handler = NextAuth(authOptions);

export { handler as GET, handler as POST };
