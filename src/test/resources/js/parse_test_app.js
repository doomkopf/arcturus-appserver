function arc_entityServices() {
        return 'user,bankAccount'
}

function arc_anyServices() {
        return null
}

function arc_user_currentVersion() {
        return 1
}

function arc_bankAccount_currentVersion() {
        return 1
}

function arc_user_defaultEntity() {
        return {
                name: '',
                bankAccount1: null,
                bankAccount2: null
        }
}

function arc_bankAccount_defaultEntity() {
        return {
                money: 0
        }
}

function arc_user_update(entity, id, deltaTime, context) {
}

function arc_bankAccount_update(entity, id, deltaTime, context) {
}

MAX_NAME_LENGTH = 8;function arc_user_setName(entity, id, requestId, requestingUserId, payload, context) {
        if (payload.name.length > MAX_NAME_LENGTH) {
                responseSender.send(requestId, { uc: 'setName', error: 'nameTooLong' })
                return
        }

        entity.name = payload.name
        context.dirty()

        responseSender.send(requestId, { uc: 'setName', error: 'ok', name: entity.name })
}function arc_bankAccount_create(entity, id, requestId, requestingUserId, payload, context) {entity.money = payload.money
        context.dirty()

        userService.send('writeBankAccount', requestingUserId, requestId, requestingUserId, { bankAccountId: id.toString() })
}

arc_user_writeBankAccount=function(entity, id, requestId, requestingUserId, payload, context) {
        if (entity.bankAccount1 != null && entity.bankAccount2 != null) {
                entity.bankAccount1 = null
                entity.bankAccount2 = null
        }

        if (entity.bankAccount1 == null) {
                entity.bankAccount1 = payload.bankAccountId
        } else {
                entity.bankAccount2 = payload.bankAccountId
        }

        context.dirty()

        responseSender.send(requestId, { uc: 'create', error: 'ok', id: payload.bankAccountId })
}

function arc_user_transferMoney(entity, id, requestId, requestingUserId, payload, context) {
        transactionManager.startTransaction({
                entities: [
                        {
                                id: entity.bankAccount1,
                                service: 'bankAccount',
                                uc: 'account1'
                        },
                        {
                                id: entity.bankAccount2,
                                service: 'bankAccount',
                                uc: 'account2'
                        }
                ]
        }, requestId, requestingUserId, payload)
}

function arc_bankAccount_account1_validate(entity, id, requestId, requestingUserId, payload) {
        if (entity.money != 100) {
                return { ok: false }
        }

        tools.sleep(500)

        return { ok: true, payload: {} }
}

function arc_bankAccount_account1_commit(entity, id, requestId, requestingUserId, payload) {
        entity.money = 50
}

arc_bankAccount_account2_validate = function(entity, id, requestId, requestingUserId, payload) {
        if (entity.money != 0) {
                return { ok: false }
        }

        return { ok: true, payload: {} }
}

function arc_bankAccount_account2_commit(entity, id, requestId, requestingUserId, payload) {
        entity.money = 50
}

function arc_bankAccount_mutationAttemptDuringTransaction(entity, id, requestId, requestingUserId, payload, context) {
        entity.money = entity.money / 2
        context.dirty()

        responseSender.send(requestId, { uc: 'mutationAttemptDuringTransaction', moneyOfFirstAccount: entity.money })
}

function arc__serviceless(requestId, requestingUserId, payload) {
        responseSender.send(requestId, { uc: 'serviceless' })
}

function arc_bankAccount_read(entity, id, requestId, requestingUserId, payload, context) {
        responseSender.send(requestId, { uc: 'read', money: entity.money })
}