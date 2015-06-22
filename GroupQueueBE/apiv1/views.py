from rest_framework import viewsets
from apiv1.serializers import ListenerSerializer, QueueGroupSerializer, UserSerializer
from apiv1.models import Listener, QueueGroup
from django.contrib.auth.models import User, Group
from rest_framework.decorators import list_route
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

import uuid

# Create your views here.
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

class QueueGroupViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows groups to be viewed or edited.
    """
    queryset = QueueGroup.objects.all()
    serializer_class = QueueGroupSerializer

    @list_route(methods=['get'], permission_classes=[IsAuthenticated], url_path='create-group')
    def create_group(self, request):
        new_group = QueueGroup.objects.create(group_id=(str(uuid.uuid4().get_hex().upper()[0:6])))
        return Response(self.get_serializer(new_group).data)
