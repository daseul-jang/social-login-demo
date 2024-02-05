import KakaoLogin from '@/components/features/socialLogin/KakaoLogin';
import { getServerSession } from 'next-auth';
import { authOptions } from './api/auth/[...nextauth]/route';

export default async function Home() {
  const session = await getServerSession(authOptions);
  const user = session?.user;

  console.log('세션유저!!!!!!!!');
  console.log(user);

  return (
    <main className='flex min-h-screen flex-col items-center justify-center p-24'>
      <KakaoLogin />
    </main>
  );
}
