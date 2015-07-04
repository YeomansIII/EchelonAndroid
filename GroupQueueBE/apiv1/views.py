from rest_framework import viewsets, generics
from apiv1.serializers import ListenerSerializer, QueueGroupSerializer, UserSerializer
from apiv1.models import Listener, QueueGroup
from django.contrib.auth.models import User, Group
from rest_framework.decorators import list_route, api_view
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.reverse import reverse

import json

# Create your views here.
@api_view(('GET',))
def api_root(request, format=None):
    return Response({
        'users': reverse('apiv1:user-list', request=request, format=format),
        'listeners': reverse('apiv1:listener-list', request=request, format=format),
        'queuegroups': reverse('apiv1:queuegroup-list', request=request, format=format),
    })


class UserViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows users to be viewed or edited.
    """
    queryset = User.objects.all()
    serializer_class = UserSerializer
    #lookup_field = 'username'

class ListenerViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows users to be viewed or edited.
    """
    queryset = Listener.objects.all()
    serializer_class = ListenerSerializer
    #lookup_field = 'user__username'

    @list_route(methods=['get'], permission_classes=[IsAuthenticated], url_path='my-user-info')
    def my_user_info(self, request):
        listener = Listener.objects.get(user=request.user)
        return Response(self.get_serializer(listener).data)

class GetListenerView(generics.RetrieveAPIView):
    """
    Retreive a single Listener
    """
    model = Listener
    serializer_class = ListenerSerializer
    lookup_field="user__username"
    view_name="apiv1:listener-detail"

    def get_queryset(self):
        username = self.kwargs['user__username']
        return Listener.objects.filter(user__username = username)

class QueueGroupViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows groups to be viewed or edited.
    """
    queryset = QueueGroup.objects.all()
    serializer_class = QueueGroupSerializer

    @list_route(methods=['put'], permission_classes=[IsAuthenticated], url_path='activate-my-group')
    def activate_group(self, request):
        listener = Listener.objects.get(user=request.user);
        my_group = listener.owner_of
        my_group.is_active = True
        listener.is_leader = True
        listener.active_queuegroup = my_group

        my_group.save()
        listener.save()

        return Response(self.get_serializer(my_group).data)

    @list_route(methods=['put'], permission_classes=[IsAuthenticated], url_path='join-group')
    def join_group(self, request):
        print(request.body)
        j = json.loads(request.body)

        listener = Listener.objects.get(user=request.user);
        join_group = Listener.objects.get(user__username=j['username_join']).owner_of

        if join_group.is_active:
            if listener.is_leader:
                listener.is_leader = False
            listener.active_queuegroup = join_group

            listener.save()
            return Response(self.get_serializer(join_group).data)
        else:
            data = {}
            data["join_errors"] = ["That group is not active."]
            return Response(data)
