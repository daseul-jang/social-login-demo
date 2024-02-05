'use client';

import { signIn, signOut } from 'next-auth/react';

export default function KakaoLogin() {
  return (
    <>
      <button
        onClick={() =>
          signIn('kakao', {
            redirect: false,
          })
        }
      >
        카카오 로그인
      </button>
      <button onClick={() => signOut({ callbackUrl: '/' })}>로그아웃</button>
    </>
  );
}
