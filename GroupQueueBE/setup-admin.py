from django.contrib.auth.models import User
from apiv1.models import Listener, QueueGroup

inp = input("Enter the user you want to create a qg and listener for (type 'exit' to exit)")

while inp is not "exit":
    u = User.objects.get(username='jason')
    q = QueueGroup.objects.create()
    l = Listener.objects.create(user=u, owner_of=q)
    q.save()
    l.save()
    inp = input("Enter the user you want to create a qg and listener for (type 'exit' to exit)")
