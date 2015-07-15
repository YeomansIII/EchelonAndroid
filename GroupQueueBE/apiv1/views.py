from rest_framework import viewsets, generics
from apiv1.serializers import ListenerSerializer, QueueGroupSerializer, UserSerializer
from apiv1.models import Listener, QueueGroup, QueueTrack
from django.contrib.auth.models import User, Group
from rest_framework.decorators import list_route, api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from rest_framework.reverse import reverse

import json
from gcm import GCM


SERVER = 'gcm.googleapis.com'
PORT = 5235
USERNAME = "45203521863"
API_KEY = "AIzaSyBhZLgrZpXvligcmVr19xNyN4J5hRgvJlo"
REGISTRATION_ID = "Registration Id of the target device"

# Create your views here.
@api_view(('GET',))
def api_root(request, format=None):
    return Response({
        'users': reverse('apiv1:user-list', request=request, format=format),
        'listeners': reverse('apiv1:listener-list', request=request, format=format),
        'queuegroups': reverse('apiv1:queuegroup-list', request=request, format=format),
    })

@api_view(['POST'])
@permission_classes([AllowAny])
def create_account(request):
    print(request.body)
    j = json.loads(request.body)
    u = User.objects.create(email=j['email'], username=j['username'])
    u.set_password(j['password'])
    u.save()
    q = QueueGroup.objects.create()
    q.save()
    l = Listener.objects.create(user=u, owner_of=q)
    l.save()
    return Response("account_created")
    #else:
        #return Response(serialized._errors, status=status.HTTP_400_BAD_REQUEST)


class UserViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows users to be viewed or edited.
    """
    queryset = User.objects.all()
    serializer_class = UserSerializer
    #lookup_field = 'username'

# class QueueTrackViewSet(viewsets.ModelViewSet):
#     """
#     API endpoint that allows users to be viewed or edited.
#     """
#     queryset = Listener.objects.all()
#     serializer_class = ListenerSerializer
#     #lookup_field = 'user__username'

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

    @list_route(methods=['put'], permission_classes=[IsAuthenticated], url_path='queue-song')
    def queue_song(self, request):
        print(request.body)
        j = json.loads(request.body)

        listener = Listener.objects.get(user=request.user);
        my_group = listener.active_queuegroup

        track = QueueTrack.objects.create(spotify_id=j['spotify_id'], in_queue=my_group)
        track.save()

        gcm = GCM(API_KEY)
        data = {'action': 'pull_group', 'group': my_group.pk}

        participants = Listener.objects.filter(active_queuegroup=my_group)
        print(str(participants))
        #reg_ids = []
        for part in participants:
            gcm.plaintext_request(registration_id=part.gcm_id, data=data)
            #reg_ids.append(part.gcm_id)
        #print(str(reg_ids))

        #gcm.plaintext_request(registration_ids=reg_ids, data=data)

        return Response(self.get_serializer(my_group).data)

    @list_route(methods=['put'], permission_classes=[IsAuthenticated], url_path='remove-song')
    def remove_song(self, request):
        print(request.body)
        j = json.loads(request.body)

        listener = Listener.objects.get(user=request.user);
        my_group = listener.active_queuegroup

        track = QueueTrack.objects.create(spotify_id=j['spotify_id'], in_queue=my_group)
        track.save()

        gcm = GCM(API_KEY)
        data = {'action': 'pull_group', 'group': my_group.pk}

        participants = Listener.objects.filter(active_queuegroup=my_group)
        print(str(participants))
        #reg_ids = []
        for part in participants:
            gcm.plaintext_request(registration_id=part.gcm_id, data=data)
            #reg_ids.append(part.gcm_id)
        #print(str(reg_ids))

        #gcm.plaintext_request(registration_ids=reg_ids, data=data)

        return Response(self.get_serializer(my_group).data)

    @list_route(methods=['put'], permission_classes=[IsAuthenticated], url_path='update-song')
    def update_song(self, request):
        print(request.body)
        j = json.loads(request.body)

        listener = Listener.objects.get(user=request.user);
        my_group = listener.active_queuegroup

        track = QueueTrack.objects.get(pk=j['pk'])
        if 'played' in j:
            track.played = j['played']
        track.save()

        gcm = GCM(API_KEY)
        data = {'action': 'pull_group', 'group': my_group.pk}

        participants = Listener.objects.filter(active_queuegroup=my_group)
        print(str(participants))
        #reg_ids = []
        for part in participants:
            gcm.plaintext_request(registration_id=part.gcm_id, data=data)
            #reg_ids.append(part.gcm_id)
        #print(str(reg_ids))

        #gcm.plaintext_request(registration_ids=reg_ids, data=data)

        return Response(self.get_serializer(my_group).data)


    @list_route(methods=['get'], permission_classes=[IsAuthenticated], url_path='reset-group')
    def reset_group(self, request):

        listener = Listener.objects.get(user=request.user)
        my_group = listener.active_queuegroup
        if listener.is_leader:
            listener.is_leader = False
            my_group.is_active = False

            partic = Listener.objects.filter(active_queuegroup=my_group)
            partic.update(active_queuegroup = None)

            tracks = QueueTrack.objects.filter(in_queue=my_group)
            tracks.delete()

        #my_group.save()

        listener.active_queuegroup = None
        listener.save()

        return Response(self.get_serializer(my_group).data)
