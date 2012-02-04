task :default do
  sh "ssh -p 2222 ujihisa.shiracha.net 'cd git/plusplus-lingrbot; git pull'"
  system "ssh -p 2222 ujihisa.shiracha.net 'pkill -f plusplus'"
  sh "ssh -p 2222 ujihisa.shiracha.net 'cd git/plusplus-lingrbot; nohup lein run >& nohup.out & sleep 10; sudo service apache2 restart'"
end
