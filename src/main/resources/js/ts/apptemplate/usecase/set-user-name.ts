import { responseSender, arc, FunctionContext } from "../arcturus/arcturus-types";
import { User } from "../service/user/user-service";

const MAX_NAME_LENGTH = 8;

interface SetNameRequest {
    name: string;
}

arc.entity.user.func.pub.create.setName = (user: User, id: string, requestId: number, requestingUserId: string, payload: SetNameRequest, context: FunctionContext) => {
    if (payload.name.length > MAX_NAME_LENGTH) {
        responseSender.send(requestId, { uc: 'setName', status: 'nameTooLong' });
        return;
    }

    user.name = payload.name;
    context.dirty();

    responseSender.send(requestId, { uc: 'setName', status: 'ok', name: user.name });
}