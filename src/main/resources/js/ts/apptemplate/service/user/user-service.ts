import { arc, FunctionContext } from "../../arcturus/arcturus-types";

export class User {
    constructor(
        private _name: string
    ) { }

    get name(): string {
        return this._name;
    }

    set name(name: string) {
        this._name = name;
    }
}

arc.entity.user.defaultEntity = (id: string) => {
    return new User('');
}

arc.entity.user.initEntity = (entityJson: any) => {
    return new User(entityJson._name);
}

arc.entity.user.update = (user: User, id: string, currentTimeMillis: number, deltaTime: number, context: FunctionContext) => {
}